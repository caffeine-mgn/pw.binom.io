# Binom Charset
Kotlin Library for work with charsets and strings

## Using
### Gradle
You must add repository. See [README](../README.md)
```kotlin
dependencies {
    api("pw.binom.io:charset:<version>")
}
```

## Contains
* [Charset](src/commonMain/kotlin/pw/binom/charset/Charset.kt) base interface for encode and decode string to/from bytes
* [CharsetCoder](src/commonMain/kotlin/pw/binom/charset/CharsetCoder.kt) tool for stream encode/decode string/bytes
* [CharsetEncoder](src/commonMain/kotlin/pw/binom/charset/CharsetEncoder.kt) base interface for encode string to bytes
* [CharsetDecoder](src/commonMain/kotlin/pw/binom/charset/CharsetDecoder.kt) base interface for decode bytes to string
* [Charsets](src/commonMain/kotlin/pw/binom/charset/Charsets.kt) Tool for access to all available charsets
