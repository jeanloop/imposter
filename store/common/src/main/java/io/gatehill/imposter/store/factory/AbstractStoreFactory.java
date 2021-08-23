package io.gatehill.imposter.store.factory;

import io.gatehill.imposter.store.inmem.InMemoryStore;
import io.gatehill.imposter.store.model.PrefixedKeyStore;
import io.gatehill.imposter.store.model.Store;
import io.gatehill.imposter.store.model.StoreFactory;
import io.gatehill.imposter.store.util.StoreUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

/**
 * Common store factory methods. Supports adding a prefix to keys by setting the {@link #ENV_VAR_KEY_PREFIX}
 * environment variable.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public abstract class AbstractStoreFactory implements StoreFactory {
    private static final String ENV_VAR_KEY_PREFIX = "IMPOSTER_STORE_KEY_PREFIX";
    private static final Logger LOGGER = LogManager.getLogger(AbstractStoreFactory.class);

    private final Map<String, Store> stores = newHashMap();
    private final String keyPrefix;

    public AbstractStoreFactory() {
        this.keyPrefix = ofNullable(System.getenv(ENV_VAR_KEY_PREFIX)).map(p -> p + ".").orElse("");
    }

    @Override
    public boolean hasStoreWithName(String storeName) {
        if (StoreUtil.isRequestScopedStore(storeName)) {
            return true;
        }
        return stores.containsKey(storeName);
    }

    @Override
    public Store getStoreByName(String storeName, boolean forceInMemory) {
        Store store;
        if (isNull(store = stores.get(storeName))) {
            LOGGER.trace("Initialising new store: {}", storeName);
            if (forceInMemory) {
                store = new InMemoryStore(storeName);
            } else {
                store = new PrefixedKeyStore(keyPrefix, buildNewStore(storeName));
            }
            stores.put(storeName, store);
        }
        LOGGER.trace("Got store: {} (type: {})", storeName, store.getTypeDescription());
        return store;
    }

    @Override
    public void deleteStoreByName(String storeName) {
        if (nonNull(stores.remove(storeName))) {
            LOGGER.trace("Deleted store: {}", storeName);
        }
    }

    public abstract Store buildNewStore(String storeName);
}
