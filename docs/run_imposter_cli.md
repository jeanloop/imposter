# Running Imposter with the CLI

There are lots of way to run Imposter.

### Standalone mock server

1. Using the command line client - see below
2. As a Docker container - see [Imposter Docker container](./run_imposter_docker.md)
3. As a JAR file on the JVM - see [Imposter JAR file](./run_imposter_jar.md)

### Embedded in tests

4. Embedded within your **Java/Kotlin/Scala/JVM** unit tests - see [JVM bindings](../distro/embedded/README.md) 
5. Embedded within your **JavaScript/Node.js** unit tests - see [JavaScript bindings](https://github.com/gatehill/imposter-js) 

---

## CLI Features

- Start mocks (`imposter up`)
- Generate mock configuration from OpenAPI files (`imposter scaffold`)
- Supports all [plugins](./features_plugins.md)

## Installation

> See the full [Installation](https://github.com/gatehill/imposter-cli/blob/main/docs/install.md) instructions for your system.

### Prerequisites

You must have [Docker](https://docs.docker.com/get-docker/) or a JVM installed.

### Homebrew

If you have Homebrew installed:

    brew tap gatehill/imposter
    brew install imposter

### Shell script

Or, use this one liner (macOS and Linux only):

```shell
curl -L https://raw.githubusercontent.com/gatehill/imposter-cli/main/install/install_imposter.sh | bash -
```

## Example

```shell
$ cd /path/to/config
$ imposter up

Starting server on port 8080...
Parsing configuration file: someapi-config.yaml
...
Mock server is up and running
```

## Usage

See full usage instructions on [Imposter CLI](https://github.com/gatehill/imposter-cli).

---

## What's next

- Learn how to use Imposter with the [Configuration guide](configuration.md).