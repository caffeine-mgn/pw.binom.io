# Binom File IO
Kotlin Library for work with File System.<br>
## Using
### Gradle
You must add repository. See [README](../README.md)
```groovy
dependencies {
    api "pw.binom.io:file:<version>"
}
```

## Simple using
### Write File
```kotlin
import pw.binom.asUTF8ByteArray
import pw.binom.io.file.*
import pw.binom.io.use

val file = File("file-example")
val data = "Simple Text".asUTF8ByteArray()

FileOutputStream(file, false).use {
    it.write(data, 0, data.size)
    it.flush()
}
```

### Read File
```kotlin
import pw.binom.asUTF8String
import pw.binom.io.file.*
import pw.binom.io.copyTo
import pw.binom.io.use
import pw.binom.io.ByteArrayOutputStream
val file = File("file-example")
val out = ByteArrayOutputStream()
FileInputStream(file).use {
    it.copyTo(out)
}


println("Data from File: \"${out.toByteArray().asUTF8String()}\"")
```

## Examples
[Read-Write File](../examples/read-write-file)