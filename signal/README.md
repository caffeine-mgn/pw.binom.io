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

val flag = true

private fun stop() {
    flag = false
}

fun main() {
    // Adding terminate hook
    Signal.handler { signalType: Signal.Type -> // this lambda will call when you press Ctrl+C in console 
        if (signalType.isInterrupted) {
            stop() // stopping service
        }
    }

    while (flag) {
        Thread.sleep(1000) // wait until flag is true
    }
}
```
