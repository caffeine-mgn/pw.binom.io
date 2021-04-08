package pw.binom.date

import pw.binom.date.format.toDatePattern

val dd1 = "yyyy-MM-dd".toDatePattern()
val dd2 = "yyyy-MM-dd HH:mm:ss".toDatePattern()
val dd3 = "yyyy-MM-dd HH:mm:ssXXX".toDatePattern()
val dd4 = "yyyy-MM-dd HH:mm:ss.SSSXXX".toDatePattern()
val dd5 = "yyyy-MM-dd HH:mm:ss.SSS".toDatePattern()
val dd6 = "yyyy-MM-dd HH:mm:ssXX".toDatePattern()

fun Calendar.iso8601() = dd4.toString(this)
fun Date.iso8601(timeZoneOffset: Int = Date.timeZoneOffset) = calendar(timeZoneOffset).iso8601()

fun String.parseIsoDate(defaultTimezoneOffset: Int = Date.timeZoneOffset): Date? =
    dd1.parseOrNull(this, defaultTimezoneOffset)
        ?: dd2.parseOrNull(this, defaultTimezoneOffset)
        ?: dd3.parseOrNull(this, defaultTimezoneOffset)
        ?: dd4.parseOrNull(this, defaultTimezoneOffset)
        ?: dd5.parseOrNull(this, defaultTimezoneOffset)
        ?: dd6.parseOrNull(this, defaultTimezoneOffset)