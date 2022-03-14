package pw.binom.date

import pw.binom.date.format.as2
import kotlin.math.absoluteValue

expect class Calendar {
    val year: Int

    /**
     * Timezone offset in minutes
     */
    val offset: Int

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
     * Returns date as string in GMT Timezone
     */
    override fun toString(): String

    /**
     * @param timeZoneOffset4 TimeZone offset in mintes
     */
    fun toString(timeZoneOffset4: Int): String

    /**
     * Changes current TimeZone.
     *
     * @param timeZoneOffset3 TimeZone offset in mintes
     */
    fun timeZone(timeZoneOffset3: Int): Calendar
    fun toDate(): Date
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
    timeZoneOffset: Int = this.offset
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
    timeZoneOffset: Int = this.offset
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
