package pw.binom.date.format

import pw.binom.date.Calendar
import pw.binom.date.Date
import kotlin.jvm.JvmInline

@JvmInline
value class DateFormat internal constructor(private val format: Array<Pattern>) {
    fun parseOrNull(text: String, defaultTimezoneOffset: Int = Date.systemZoneOffset): Date? {
        var year = 0
        var month = 0
        var dayOfMonth = 0
        var hours = 0
        var minutes = 0
        var seconds = 0
        var millis = 0
        var timeZoneOffset = defaultTimezoneOffset

        var index = 0
        var i = 0
        while (index < text.length) {
            if (i >= format.size) {
                return null
            }
            val size = format[i].parse(text, index) { type, value ->
                when (type) {
                    Pattern.FieldType.YEAR -> year = value
                    Pattern.FieldType.MONTH -> month = value
                    Pattern.FieldType.DAY_OF_MONTH -> dayOfMonth = value
                    Pattern.FieldType.HOURS -> hours = value
                    Pattern.FieldType.MINUTES -> minutes = value
                    Pattern.FieldType.SECONDS -> seconds = value
                    Pattern.FieldType.MILLISECOND -> millis = value
                    Pattern.FieldType.TIME_ZONE -> timeZoneOffset = value
                }
            }
            if (size == -1) {
                return null
            }
            i++
            index += size
        }

        if (i != format.size) {
            return null
        }
        return Date.internalOf(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            millis = millis,
            timeZoneOffset = timeZoneOffset,
        )
    }

    fun toString(calendar: Calendar): String {
        val sb = StringBuilder()
        format.forEach {
            sb.append(it.toString(calendar))
        }
        return sb.toString()
    }

    fun toString(date: Date, timeZoneOffset: Int = Date.systemZoneOffset): String =
        toString(date.calendar(timeZoneOffset = timeZoneOffset))

    companion object {
        internal fun parsePatternList(format: String): Array<Pattern> {
            var index = 0
            val list = ArrayList<Pattern>()
            while (index < format.length) {
                val pattern = Pattern.find(format, index) ?: throw IllegalArgumentException(
                    "Can't parse near \"${format.substring(index)}\" on column ${index + 1}"
                )
                list += pattern
                val len = pattern.len(format, index)
                index += len
            }
            return list.toTypedArray()
        }
    }
}

fun String.toDatePattern() = DateFormat(DateFormat.parsePatternList(this))