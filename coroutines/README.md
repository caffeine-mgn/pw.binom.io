# Binom Coroutines

Tools for Async work

## Using

### Gradle

You must add repository. See [README](../README.md)

```kotlin
dependencies {
    api("pw.binom.io:date:<version>")
}
```

### Documentation

#### [SimpleAsyncLock](src/commonMain/kotlin/pw/binom/coroutines/SimpleAsyncLock.kt)

Extends [AsyncLock](src/commonMain/kotlin/pw/binom/coroutines/AsyncLock.kt)<br>
Double call of method `lock` can make deadlock.<br>
Method `unlock` can be called from any scope and coroutine for unlock<br>
Example:<br>

```kotlin
val lock = SimpleAsyncLock()
val job1 = GlobalScope.launch {
    lock.synchronize {
        println("Job1 executed!")
    }
}

val job2 = GlobalScope.launch {
    lock.synchronize {
        println("Job2 executed!")
    }
}

val job3 = GlobalScope.launch {
    lock.synchronize {
        lock.synchronize { // deadlock happened
            println("Job2 executed!")
        }
    }
}
```