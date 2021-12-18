# Binom Signal

Kotlin Library for listen external OS signals

## Using

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

fun init() {
    start() //staring service

    // Adding terminate hook
    Signal.handler { signalType: Signal.Type -> //this lambda will call when you press Ctrl+C in console 
        if (signalType.isInterrupted) {
            stop() //stopping service
        }
    }
}
```