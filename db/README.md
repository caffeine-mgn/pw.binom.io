# Binom Tarantool Client
Kotlin Library for use [Tarantool](https://www.tarantool.io/)

## Usage
### Gradle
You must add repository. See [README](../README.md)
```groovy
dependencies {
    api "pw.binom.io:tarantool-client:<version>"
}
```

## Example
```kotlin
val connection = TarantoolConnection.connect(
    address = NetworkAddress.Immutable(host="127.0.0.1", port=3301),
    userName = "admin",
    password = "admin"
)
connection.ping()
connection.asyncClose() // connection implements `AsyncCloseable` and can be used like `connection.use{ connection ->}`
```