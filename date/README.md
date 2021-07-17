# Binom Date

Kotlin Library for work with Date

## Date pattern descriptions
|Pattern|Description|Example|
|----|---|-----|
|(patterns separated with \|)|Can be use |dfdfdf|

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
val dp = "yyyy-MM-dd[(T| )HH:mm[:ss[.(SSS|SS)]][(XXX|XX|X)]]".toDatePattern()

//Parsing
val date = dp.parseOrNull(text = inputDateStr, defaultTimezoneOffset = Date.systemZoneOffset)

//Formatting
val dateStr = dp.toString(date = date, timeZoneOffset = Date.systemZoneOffset)

//Testing
assertEquals(inputDateStr, dateStr)
```