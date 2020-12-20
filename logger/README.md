# Binom JSON

Logger Kotlin Library

## Using

### Gradle

You must add repository. See [README](../README.md)

```groovy
dependencies {
    api "pw.binom.io:logger:<version>"
}
```

# Example

## Simple

```kotlin
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.logger.severe
import pw.binom.logger.warn

fun main() {
    val logger = Logger.getLogger("Main")
    logger.info("Hello from Logger")
}
```

## Custom Logger Level

```kotlin
private object FATAL_LEVEL : Logger.Level {
    override val name: String
        get() = "F"
    override val priority: UInt
        get() = UInt.MAX_VALUE
}

val Logger.Companion.FATAL: Logger.Level
    get() = FATAL_LEVEL

fun Logger.fatal(text: String) {
    log(Logger.FATAL, text, null)
}
fun main() {
    val logger = Logger.getLogger("Main")
    logger.fatal("Can't run application")
}
```

## Custom Logger Handler

```kotlin
fun main() {
    val myHandler = Logger.Logger = { logger: Logger, level: Level, text: String?, exception: Throwable? ->
        println("logger: [${logger.pkg}], level: ${level.name}: $text")
    }
    Logger.global.handler = myHandler
    Logger.getLogger("MyLogger").handler = myHandler
}
```

Also, You can write Handler for store logs to remote storage, like [Graylog](https://www.graylog.org) or [Kibana](https://www.elastic.co/kibana)