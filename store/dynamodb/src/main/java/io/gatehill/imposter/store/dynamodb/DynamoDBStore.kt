/*
 * Copyright (c) 2016-2021.
 *
 * This file is part of Imposter.
 *
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as
 * defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software: Imposter
 *
 * License: GNU Lesser General Public License version 3
 *
 * Licensor: Peter Cornish
 *
 * Imposter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Imposter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Imposter.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.gatehill.imposter.store.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import io.gatehill.imposter.store.model.Store
import org.apache.logging.log4j.LogManager

/**
 * Store implementation using DynamoDB.
 *
 * @author Pete Cornish
 */
class DynamoDBStore(
    override val storeName: String,
    private val ddb: AmazonDynamoDB,
    private val tableName: String
) : Store {
    override val typeDescription = "dynamodb"
    private val logger = LogManager.getLogger(DynamoDBStore::class.java)

    init {
        logger.debug("Initialised DynamoDB store: $storeName using table: $tableName")
    }

    override fun save(key: String, value: Any?) {
        logger.trace("Saving item with key: {} to store: {}", key, storeName)
        val valueAttribute = AttributeValue().apply {
            when (value) {
                is String -> withS(value.toString())
                is Number -> withN(value.toString())
                is Boolean -> withBOOL(value.toString().toBoolean())
                null -> withNULL(true)
                else -> withS(value.toString())
            }
        }

        ddb.putItem(
            PutItemRequest().withTableName(tableName).withItem(
                mapOf(
                    "StoreName" to AttributeValue().withS(storeName),
                    "Key" to AttributeValue().withS(key),
                    "Value" to valueAttribute
                )
            )
        )
    }

    override fun <T> load(key: String): T? {
        logger.trace("Loading item with key: {} from store: {}", key, storeName)
        val result = ddb.getItem(
            GetItemRequest().withTableName(tableName).withKey(
                mapOf(
                    "StoreName" to AttributeValue().withS(storeName),
                    "Key" to AttributeValue().withS(key)
                )
            )
        )

        return result?.item?.let {
            val (_, value) = destructure<T>(it)
            return@let value
        }
    }

    override fun delete(key: String) {
        logger.trace("Deleting item with key: {} from store: {}", key, storeName)
        ddb.deleteItem(
            DeleteItemRequest().withTableName(tableName).withKey(
                mapOf(
                    "StoreName" to AttributeValue().withS(storeName),
                    "Key" to AttributeValue().withS(key)
                )
            )
        )
    }

    override fun loadAll(): Map<String, Any> {
        val queryResult = scanStore()
        logger.trace("Loading all items in store: {}", storeName)
        return queryResult.items.associate { destructure(it) }
    }

    private fun scanStore() = ddb.scan(
        ScanRequest()
            .withTableName(tableName)
            .withFilterExpression("StoreName = :storeName")
            .withExpressionAttributeValues(mapOf(":storeName" to AttributeValue().withS(storeName)))
    )

    override fun hasItemWithKey(key: String): Boolean {
        logger.trace("Checking for item with key: {} in store: {}", key, storeName)
        return load<Any>(key) != null
    }

    override fun count(): Int {
        val count = scanStore().count
        logger.trace("Returning item count {} from store: {}", count, storeName)
        return count
    }

    private fun <T> destructure(attributeItem: Map<String, AttributeValue>): Pair<String, T> {
        val attributeKey = attributeItem.getValue("Key").s
        val attributeValue = attributeItem.getValue("Value")
        val rawValue = attributeValue.s ?: attributeValue.b ?: attributeValue.n
        @Suppress("UNCHECKED_CAST")
        return attributeKey to rawValue as T
    }
}
