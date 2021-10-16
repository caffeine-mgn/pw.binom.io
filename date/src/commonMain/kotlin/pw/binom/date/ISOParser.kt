package pw.binom.date

import pw.binom.date.format.toDatePattern
import kotlin.native.concurrent.SharedImmutable

//val iso = "yyyy-MM-dd[( |'T')HH:mm:ss[.(SS|SSS)][(X|XX|XXX)]]".toDatePattern()
@SharedImmutable
private val iso = "yyyy-MM-dd[('T'| )HH:mm[:ss[.(SSS|SS|S)]]][(XXX|XX|X)]".toDatePattern()
//private val dp1 = "yyyy-MM-dd".toDatePattern()
//private val dp2 = "yyyy-MM-dd HH:mm:ss".toDatePattern()
//private val dp3 = "yyyy-MM-dd HH:mm:ssXXX".toDatePattern()
//private val dp4 = "yyyy-MM-dd HH:mm:ss.SSSXXX".toDatePattern()
//private val dp5 = "yyyy-MM-dd HH:mm:ss.SSS".toDatePattern()
//private val dp6 = "yyyy-MM-dd HH:mm:ss.SS".toDatePattern()
//private val dp7 = "yyyy-MM-dd HH:mm:ssXX".toDatePattern()
//private val dp8 = "yyyy-MM-dd HH:mm:ss.SSSXX".toDatePattern()
//private val dp9 = "yyyy-MM-dd HH:mm:ss.SSX".toDatePattern()
//private val dp10 = "yyyy-MM-dd HH:mm:ss.SSSX".toDatePattern()
//private val dp11 = "yyyy-MM-dd HH:mm:ssX".toDatePattern()
//
//private val dp12 = "yyyy-MM-dd'T'HH:mm:ss".toDatePattern()
//private val dp13 = "yyyy-MM-dd'T'HH:mm:ssXXX".toDatePattern()
//private val dp14 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".toDatePattern()
//private val dp15 = "yyyy-MM-dd'T'HH:mm:ss.SSS".toDatePattern()
//private val dp16 = "yyyy-MM-dd'T'HH:mm:ss.SS".toDatePattern()
//private val dp17 = "yyyy-MM-dd'T'HH:mm:ssXX".toDatePattern()
//private val dp18 = "yyyy-MM-dd'T'HH:mm:ss.SSSXX".toDatePattern()
//private val dp19 = "yyyy-MM-dd'T'HH:mm:ss.SSX".toDatePattern()
//private val dp20 = "yyyy-MM-dd'T'HH:mm:ss.SSSX".toDatePattern()
//private val dp21 = "yyyy-MM-dd'T'HH:mm:ssX".toDatePattern()

/**
 * Converts current Calendar to ISO-8601 using format `yyyy-MM-dd HH:mm:ss.SSSXXX`
 */
fun Calendar.iso8601() = iso.toString(this)
/**
 * Converts current Date to ISO-8601 using format `yyyy-MM-dd HH:mm:ss.SSSXXX`
 * @param timeZoneOffset default timezone
 */
fun Date.iso8601(timeZoneOffset: Int = Date.systemZoneOffset) = calendar(timeZoneOffset).iso8601()

/**
 * Parse current string to Date using [defaultTimezoneOffset]
 * Support ISO-8601 formats:
 * * yyyy-MM-dd
 * * yyyy-MM-dd HH:mm:ss
 * * yyyy-MM-dd HH:mm:ssXXX
 * * yyyy-MM-dd HH:mm:ss.SSSXXX
 * * yyyy-MM-dd HH:mm:ss.SSS
 * * yyyy-MM-dd HH:mm:ss.SS
 * * yyyy-MM-dd HH:mm:ssXX
 * * yyyy-MM-dd HH:mm:ss.SSSXX
 * * yyyy-MM-dd HH:mm:ss.SSX
 * * yyyy-MM-dd HH:mm:ss.SSSX
 * * yyyy-MM-dd HH:mm:ssX
 *
 * * yyyy-MM-dd'T'HH:mm:ss
 * * yyyy-MM-dd'T'HH:mm:ssXXX
 * * yyyy-MM-dd'T'HH:mm:ss.SSSXXX
 * * yyyy-MM-dd'T'HH:mm:ss.SSS
 * * yyyy-MM-dd'T'HH:mm:ss.SS
 * * yyyy-MM-dd'T'HH:mm:ssXX
 * * yyyy-MM-dd'T'HH:mm:ss.SSSXX
 * * yyyy-MM-dd'T'HH:mm:ss.SSX
 * * yyyy-MM-dd'T'HH:mm:ss.SSSX
 * * yyyy-MM-dd'T'HH:mm:ssX
 */
fun String.parseIso8601Date(defaultTimezoneOffset: Int = Date.systemZoneOffset): Date? =
    iso.parseOrNull(this,defaultTimezoneOffset)
//    dp1.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp2.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp3.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp4.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp5.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp6.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp7.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp8.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp9.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp10.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp11.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp12.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp13.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp14.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp15.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp16.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp17.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp18.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp19.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp20.parseOrNull(this, defaultTimezoneOffset)
//        ?: dp21.parseOrNull(this, defaultTimezoneOffset)