package pw.binom.date

import pw.binom.date.format.toDatePattern

private val dp1 = "yyyy-MM-dd".toDatePattern()
private val dp2 = "yyyy-MM-dd HH:mm:ss".toDatePattern()
private val dp3 = "yyyy-MM-dd HH:mm:ssXXX".toDatePattern()
private val dp4 = "yyyy-MM-dd HH:mm:ss.SSSXXX".toDatePattern()
private val dp5 = "yyyy-MM-dd HH:mm:ss.SSS".toDatePattern()
private val dp6 = "yyyy-MM-dd HH:mm:ssXX".toDatePattern()

fun Calendar.iso8601() = dp4.toString(this)
fun Date.iso8601(timeZoneOffset: Int = Date.systemZoneOffset) = calendar(timeZoneOffset).iso8601()

fun String.parseIso8601Date(defaultTimezoneOffset: Int = Date.systemZoneOffset): Date? =
    dp1.parseOrNull(this, defaultTimezoneOffset)
        ?: dp2.parseOrNull(this, defaultTimezoneOffset)
        ?: dp3.parseOrNull(this, defaultTimezoneOffset)
        ?: dp4.parseOrNull(this, defaultTimezoneOffset)
        ?: dp5.parseOrNull(this, defaultTimezoneOffset)
        ?: dp6.parseOrNull(this, defaultTimezoneOffset)