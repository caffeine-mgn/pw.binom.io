# Binom Date

Kotlin Library for work with Date

## Using

### Gradle

You must add repository. See [README](../README.md)

```groovy
dependencies {
    api "pw.binom.io:date:<version>"
}
```

## Example

```kotlin
val inputDateStr = "2021-03-29 10:17:33+00:00"

// Make date pattern
val dp = "yyyy-MM-dd HH:mm:ss.SSSXXX".toDatePattern()

//Parsing
val date = dp.parseOrNull(text = inputDateStr, defaultTimezoneOffset = Date.systemZoneOffset)

//Formatting
val dateStr = dp.toString(date = date, timeZoneOffset = Date.systemZoneOffset)

//Testing
assertEquals(inputDateStr, dateStr)
```