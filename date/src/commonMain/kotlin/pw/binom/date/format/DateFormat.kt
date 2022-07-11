package pw.binom.date.format

import pw.binom.date.Calendar
import pw.binom.date.DateTime
import kotlin.jvm.JvmInline

@JvmInline
value class DateFormat internal constructor(internal val format: Array<Pattern>) {
    data class DateFormatParseResult(val format: DateFormat, val length: Int)
    data class ParseResult(val dateTime: DateTime, val length: Int)

    val length
        get() = format.sumOf { it.patternLength }

    fun parseOrNull(text: String, defaultTimezoneOffset: Int = DateTime.systemZoneOffset): DateTime? =
        parse3(
            text = text,
            defaultTimezoneOffset = defaultTimezoneOffset,
        )?.dateTime

    override fun toString(): String = format.joinToString(separator = "")
    internal fun parse2(
        text: String,
        defaultTimezoneOffset: Int = DateTime.systemZoneOffset,
        returnNullOnEof: Boolean = true,
        position: Int = 0,
        set: ((Pattern.FieldType, Int) -> Unit)?
    ): Int {
        var index = position
        var i = 0
        while (index < text.length) {
            if (i >= format.size) {
                if (returnNullOnEof) {
                    return -1
                } else {
                    break
                }
            }
            val size = format[i].parse(
                text = text,
                position = index,
                defaultTimezoneOffset = defaultTimezoneOffset,
                set = set,
            )
            if (size == -1) {
                return -1
            }
            i++
            index += size
        }
        if (i < format.size) {
            for (f in i until format.size) {
                if (format[f] !is Pattern.Optional) {
                    return -1
                }
            }
        }
        return index - position
    }

    private fun parse3(
        text: String,
        defaultTimezoneOffset: Int = DateTime.systemZoneOffset,
        returnNullOnEof: Boolean = true,
        position: Int = 0,
    ): ParseResult? {
        var year = 1970
        var month = 1
        var dayOfMonth = 1
        var hours = 0
        var minutes = 0
        var seconds = 0
        var millis = 0
        var timeZoneOffset = defaultTimezoneOffset
        val l = parse2(
            text = text,
            defaultTimezoneOffset = defaultTimezoneOffset,
            returnNullOnEof = returnNullOnEof,
            position = position,
        ) { type, value ->
            when (type) {
                Pattern.FieldType.YEAR -> year = value
                Pattern.FieldType.MONTH -> month = value
                Pattern.FieldType.DAY_OF_MONTH -> dayOfMonth = value
                Pattern.FieldType.HOURS -> hours = value
                Pattern.FieldType.MINUTES -> minutes = value
                Pattern.FieldType.SECONDS -> seconds = value
                Pattern.FieldType.MILLISECOND -> millis = value
                Pattern.FieldType.TIME_ZONE -> timeZoneOffset = value
                Pattern.FieldType.DAY_OF_WEAK -> {}
            }
        }
        if (l == -1) {
            return null
        }
        return ParseResult(
            dateTime = DateTime.internalOf(
                year = year,
                month = month,
                dayOfMonth = dayOfMonth,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                millis = millis,
                timeZoneOffset = timeZoneOffset,
            ),
            length = l
        )
    }

    fun toString(calendar: Calendar): String {
        val sb = StringBuilder()
        format.forEach {
            sb.append(it.toString(calendar))
        }
        return sb.toString()
    }

    fun toString(date: DateTime, timeZoneOffset: Int = DateTime.systemZoneOffset): String =
        toString(date.calendar(timeZoneOffset = timeZoneOffset))

    companion object {
        internal fun parsePatternList(format: String, position: Int = 0): DateFormatParseResult {
            var index = position
            val list = ArrayList<Pattern>()
            while (index < format.length) {
                val pattern = Pattern.find(format, index) ?: throw IllegalArgumentException(
                    "Can't parse near \"${format.substring(index)}\" on column ${index + 1}"
                )
                list += pattern
                val len = pattern.patternLength
                index += len
            }
            return DateFormatParseResult(DateFormat(list.toTypedArray()), index)
        }
    }
}

fun String.toDatePattern() = DateFormat.parsePatternList(this).format
