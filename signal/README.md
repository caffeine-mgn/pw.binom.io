# Binom Signal

Kotlin Library for listen external OS signals

## Using

For work on JVM platform you should run your program with `-Xrs` flag

### Gradle

You must add repository. See [README](../README.md)

```groovy
dependencies {
    api "pw.binom.io:signal:<version>"
}
```

## Example

```kotlin
import pw.binom.process.Signal

private fun start() {
    // Starts your service
}

private fun stop() {
    //Stops your service
}

fun main() {
    start() //staring service

    // Adding terminate hook
    Signal.handler { signalType: Signal.Type -> //this lambda will call when you press Ctrl+C in console 
        if (signalType.isInterrupted) {
            stop() //stopping service
        }
    }

    while (true) {
        Thread.sleep(1000)
    }
}
```