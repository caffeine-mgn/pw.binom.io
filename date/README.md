# Binom Date

Kotlin Library for work with Date

## Date pattern descriptions
|Pattern|Description|Example|
|----|---|-----|
|`(`patterns separated with "\|"`)`|Can be use parse one of pattern in side `(` and `)`|`yyyy-MM-dd( \|T)HH:mm:ss`. In this cast date and time can be separated one of two chars ` ` or `T`. Both `2021-01-05T11:55:37` and `2021-01-05 11:55:37` is valid|
|`[`optional pattern`]`|Uses for define some part of pattern that can be lost|`HH:mm[:ss]`. Part with seconds can be lost, and it's valid input data|
|`yyyy`|Year in 4 numbers|`2021`|
|`MMM`|Month name in 3 chars|`Feb`|
|`MM`|Month number. January - 01, December - 12|`03`|
|`dd`|Day of month. First day of month is 1|`15`|
|`HH`|Hours on 24 format|`23`|
|`mm`|Minutes|`57`|
|`EEE`|3 chars name of day of week. Uses only for converting date to string|`Tue`|
|`u`|Number day of week. 1 - Sunday, 7 - Saturday. Uses only for converting date to string|`3`|
|`ss`|Number of seconds|`44`|
|SSS|3 number of milliseconds|`035`|
|SS|2 number of milliseconds|`44`|
|XXX|Timezone|`-07:00`|
|XX|Timezone|`-0700`|
|X|Timezone|`-07`|
|`,`|Char `,`|`,`|
|`-`|Char `-`|`-`|
|`/`|Char `/`|`/`|
|`:`|Char `:`|`:`|
|`.`|Char `.`|`.`|
|` ` (space)|Char ` `|` `|

Also, you can include in side `(...)` or `[...]` other `(...)` or `[...]` pattern. For example
`yyyy-MM-dd[('T'| )HH:mm[.(SSS|SS)][(XXX|XX|X)]]`.


Month name using in `MMM` pattern

|Value|Month name|
|---|---|
|`Jan`|January|
|`Feb`|February|
|`Mar`|March|
|`Apr`|April|
|`May`|May|
|`Jun`|June|
|`Jul`|July|
|`Aug`|August|
|`Sep`|September|
|`Oct`|October|
|`Nov`|November|
|`Dec`|December|

Week days using in `EEE` pattern
|Value|Week day name|
|---|---|
|`Sun`|Sunday|
|`Mon`|Monday|
|`Tue`|Tuesday|
|`Wed`|Wednesday|
|`Thu`|Thursday|
|`Fri`|Friday|
|`Sat`|Saturday|

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