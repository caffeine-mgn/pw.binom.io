package pw.binom.date

import pw.binom.date.format.as2
import kotlin.js.JsName
import kotlin.math.absoluteValue

expect class Calendar {
    val year: Int

    /**
     * Month, from 1 (January) to 12 (December)
     */
    val month: Int

    /**
     * Day of month, first day of month is 1
     */
    val dayOfMonth: Int
    val dayOfWeek: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val millisecond: Int
    val date: Date

    /**
     * Timezone offset in minutes
     */
    @JsName("tzo")
    val timeZoneOffset: Int

    /**
     * Returns date as string in GMT Timezone
     */
    override fun toString(): String

    /**
     * @param timeZoneOffset TimeZone offset in mintes
     */
    fun toString(timeZoneOffset: Int=this.timeZoneOffset): String

    /**
     * Changes current TimeZone.
     *
     * @param timeZoneOffset TimeZone offset in mintes
     */
    fun timeZone(timeZoneOffset: Int): Calendar
    fun toDate():Date
}

/**
 * @param offset Timezone Offset in minutes
 */
internal fun timeZoneOffsetToString(offset: Int): String {
    if (offset == 0) {
        return ""
    }
    val h = offset / 60
    val m = offset - h * 60
    return "${if (offset < 0) "-" else "+"}${h.absoluteValue.as2()}:${m.absoluteValue.as2()}"
}

internal fun asStringRfc822(calc: Calendar, timeZone: String): String =
    calc.rfc822()

fun Calendar.copy(
    year: Int = this.year,
    month: Int = this.month,
    dayOfMonth: Int = this.dayOfMonth,
    hours: Int = this.hours,
    minutes: Int = this.minutes,
    seconds: Int = this.seconds,
    millis: Int = this.millisecond,
    timeZoneOffset: Int = this.timeZoneOffset
) = date(
    year = year,
    month = month,
    dayOfMonth = dayOfMonth,
    hours = hours,
    minutes = minutes,
    seconds = seconds,
    millis = millis,
    timeZoneOffset = timeZoneOffset,
).calendar(timeZoneOffset)

fun Calendar.date(
    year: Int = this.year,
    month: Int = this.month,
    dayOfMonth: Int = this.dayOfMonth,
    hours: Int = this.hours,
    minutes: Int = this.minutes,
    seconds: Int = this.seconds,
    millis: Int = this.millisecond,
    timeZoneOffset: Int = this.timeZoneOffset
) =
    Date.of(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        millis = millis,
        timeZoneOffset = timeZoneOffset,
    )