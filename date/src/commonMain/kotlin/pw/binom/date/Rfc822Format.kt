package pw.binom.date

import pw.binom.date.format.toDatePattern

private val dd7 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'".toDatePattern()
//private val dd7 = "EEE, dd MMM yyyy HH:mm:ss".toDatePattern()
fun Calendar.rfc822() = dd7.toString(timeZone(0))
fun Date.rfc822() = calendar(0).rfc822()
fun String.parseRfc822Date(): Date? =
    dd7.parseOrNull(this, defaultTimezoneOffset = 0)