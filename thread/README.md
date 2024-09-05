# Binom Thread
Kotlin Library for work with threads

## Using
### Gradle
You must add repository. See [README](../README.md)
```groovy
dependencies {
    api "pw.binom.io:thread:<version>"
}
```

## Contains
* [Thread](src/commonMain/kotlin/pw/binom/thread/Thread.kt) for run new thread
* [ThreadLocal](src/commonMain/kotlin/pw/binom/thread/ThreadLocal.kt) for storage some data in current thread  
* [SingleThreadExecutorService](src/commonMain/kotlin/pw/binom/thread/SingleThreadExecutorService.kt) for run several tasks in thread pool
* [FixedThreadExecutorService](src/commonMain/kotlin/pw/binom/thread/FixedThreadExecutorService.kt) for run several tasks in thread pool
* [UncaughtExceptionHandler](src/commonMain/kotlin/pw/binom/thread/UncaughtExceptionHandler.kt) for catch exceptions in threads

## Example

### Start thread via function
```kotlin
Thread {
    Thread.sleep(1000)
}.start()
```
or
```kotlin
Thread(name = "My-Thread") {
    Thread.sleep(1000)
}.start()
``` 

### Start thread via class
```kotlin
class MyThread : Thread() {
    override fun run() {
        Thread.sleep(1000)
    }
}

val thread = MyThread()
thread.start()
thread.join()
```

### Touch current thread
```kotlin
println("Current thread id is ${Thread.currentThread.id}")
```

### ThreadLocal
```kotlin
val local = ThreadLocal<Int>()
local.set(0)

val thread = Thread {
    local.set(1)
}
thread.start()
thread.join()
println(local.get()) // prints "0"
```
