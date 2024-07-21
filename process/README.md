# Library for run process and get access to input/output of process

# Example
```kotlin
val process = ProcessStarter.create("/bin/ls").start()
process.join()
println("Exit code: ${process.exitStatus}")
```
