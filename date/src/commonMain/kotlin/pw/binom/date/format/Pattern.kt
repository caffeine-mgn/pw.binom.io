package pw.binom.date.format

import pw.binom.date.Calendar
import kotlin.math.absoluteValue

internal sealed interface Pattern {
    companion object {
        fun find(format: String, position: Int): Pattern? {
            val or = Or.parse(text = format, position)
            if (or != null) {
                return or
            }
            val optional = Optional.parse(text = format, position)
            if (optional != null) {
                return optional
            }
            return when {
                CustomText.find(format, position) -> CustomText(format, position)
                yyyy.find(format, position) -> yyyy
                MMM.find(format, position) -> MMM
                MM.find(format, position) -> MM
                dd.find(format, position) -> dd
                HH.find(format, position) -> HH
                mm_.find(format, position) -> mm_
                EEE.find(format, position) -> EEE
                COMMA.find(format, position) -> COMMA
                u.find(format, position) -> u
                ss.find(format, position) -> ss
                SSS.find(format, position) -> SSS
                SSm.find(format, position) -> SSm
                XXX.find(format, position) -> XXX
                XX.find(format, position) -> XX
                X.find(format, position) -> X
                Z.find(format, position) -> Z
                MINUS.find(format, position) -> MINUS
                SLASH.find(format, position) -> SLASH
                DOUBLE_POINT.find(format, position) -> DOUBLE_POINT
                POINT.find(format, position) -> POINT
                SPACE.find(format, position) -> SPACE
                else -> null
            }
        }
    }

    /**
     * Returns string length of pattern data. For example:
     *
     * ```kotlin
     * val txt = "yyyy HH:mm"
     * val pattern = txt.toDatePattern()
     * val year = pattern.format[0]
     * val space = pattern.format[1]
     * val hh = pattern.format[2]
     * val dots = pattern.format[3]
     * val mm = pattern.format[4]
     * println(year.patternLength) // will print 4
     * println(space.patternLength) // will print 1
     * println(hh.patternLength) // will print 2
     * println(dots.patternLength) // will print 1
     * println(mm.patternLength) // will print 2
     * ```
     */
    val patternLength: Int

    /**
     * Parse [text] on [position]. If text have valid value will call [set] for change date part of result
     * @return size of parsed input [text]
     */
    fun parse(text: String, position: Int, defaultTimezoneOffset: Int, set: ((FieldType, Int) -> Unit)?): Int
    fun toString(calendar: Calendar): String

    enum class FieldType {
        YEAR, MONTH, DAY_OF_MONTH, DAY_OF_WEAK, HOURS, MINUTES, SECONDS, MILLISECOND, TIME_ZONE
    }

    class Or(val formats: Array<DateFormat>) : Pattern {

        companion object {
            fun parse(text: String, position: Int): Or? {
                if (text[position] != '(') {
                    return null
                }
                var i = position
                var c = 1
                while (true) {
                    i++
                    if (i >= text.length) {
                        return null
                    }
                    if (text[i] == '(') {
                        c++
                    }
                    if (text[i] == ')') {
                        c--
                        if (c == 0) {
                            break
                        }
                    }
                }
                val subexpression = text.substring(position + 1, i)
                val patterns = subexpression.split('|').map {
                    DateFormat.parsePatternList(it).format
                }
                return Or(patterns.toTypedArray())
            }
        }

        override val patternLength: Int =
            formats.sumOf { it.length } + (if (formats.isEmpty()) 0 else (formats.size - 1)) + 2

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            for (f in formats) {
                val searchResult = f.parse2(
                    text = text,
                    position = position,
                    defaultTimezoneOffset = defaultTimezoneOffset,
                    returnNullOnEof = false,
                    set = null,
                )
                if (searchResult == -1) {
                    continue
                }
                return f.parse2(
                    text = text,
                    position = position,
                    defaultTimezoneOffset = defaultTimezoneOffset,
                    returnNullOnEof = false,
                    set = set,
                )
            }
            return 0
        }

        override fun toString(calendar: Calendar): String {
            if (formats.isEmpty()) {
                return ""
            }
            return formats[0].toString(calendar)
        }
        override fun toString(): String = "("+formats.joinToString("|")+")"
    }

    class Optional(val format: DateFormat) : Pattern {
        override val patternLength: Int = format.format.sumOf { it.patternLength } + 2


        companion object {
            fun parse(text: String, position: Int): Optional? {
                if (text[position] != '[') {
                    return null
                }
                var i = position
                var c = 1
                while (true) {
                    i++
                    if (i >= text.length) {
                        return null
                    }
                    if (text[i] == '[') {
                        c++
                    }
                    if (text[i] == ']') {
                        c--
                        if (c == 0) {
                            break
                        }
                    }
                }
                val bb = text.substring(position + 1, i)
                val cc = DateFormat.parsePatternList(bb).format
                return Optional(cc)
            }
        }

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            val d = format.parse2(
                text = text,
                position = position,
                defaultTimezoneOffset = defaultTimezoneOffset,
                returnNullOnEof = false,
                set = null
            )
            if (d == -1) {
                return 0
            }
            val r = format.parse2(
                text = text,
                position = position,
                defaultTimezoneOffset = defaultTimezoneOffset,
                returnNullOnEof = false,
                set = set
            )
            if (r != d) {
                throw IllegalStateException()
            }
            return r
        }

        override fun toString(calendar: Calendar): String =
            format.toString(calendar)

        override fun toString(): String = "[$format]"

    }

    /**
     * Year. Example: "2021"
     */
    object yyyy : Pattern {
        fun find(text: String, position: Int) =
            text.regionMatches(position, "yyyy", 0, patternLength)

        override val patternLength: Int
            get() = 4

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 4 > text.length) {
                return -1
            }
            val year = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.YEAR, year)
            return 4
        }

        override fun toString(calendar: Calendar): String = calendar.year.as4()

        override fun toString(): String = "yyyy"
    }

    /**
     * Month. Example: "04"
     */
    object MM : Pattern {

        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "MM", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val year = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.MONTH, year)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.month.as2()
        override fun toString(): String = "MM"
    }

    /**
     * Day of month. Example: "15"
     */
    object dd : Pattern {

        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "dd", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val year = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.DAY_OF_MONTH, year)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.dayOfMonth.as2()

        override fun toString(): String = "dd"
    }

    class CustomText(pattern: String, val start: Int) : Pattern {

        override val patternLength: Int = pattern.indexOf("'", start + 1) - start + 1

        companion object {
            fun find(text: String, position: Int): Boolean =
                text[position] == '\'' && (text.indexOf("'", position + 1) != -1)
        }

        val text = pattern.substring(start + 1, start + patternLength - 1)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            this.text.length

        override fun toString(calendar: Calendar): String =
            text

        override fun toString(): String = "'$text'"
    }

    /**
     * Hours. Example: "31"
     */
    object HH : Pattern {
        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "HH", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.HOURS, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.hours.as2()

        override fun toString(): String = "HH"
    }

    /**
     * Minutes. Example: "31"
     */
    object mm_ : Pattern {
        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "mm", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.MINUTES, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.minutes.as2()

        override fun toString(): String = "mm"
    }

    //Seconds. Example: "31"
    object ss : Pattern {
        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "ss", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.SECONDS, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.seconds.as2()

        override fun toString(): String = "ss"
    }

    //Day of week. Example: "Mon"
    object EEE : Pattern {
        override val patternLength: Int
            get() = 3

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "EEE", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val num = when {
                text.regionMatches(position, "Sun", 0, patternLength) -> 0
                text.regionMatches(position, "Mon", 0, patternLength) -> 1
                text.regionMatches(position, "Tue", 0, patternLength) -> 2
                text.regionMatches(position, "Wed", 0, patternLength) -> 3
                text.regionMatches(position, "Thu", 0, patternLength) -> 4
                text.regionMatches(position, "Fri", 0, patternLength) -> 5
                text.regionMatches(position, "Sat", 0, patternLength) -> 6
                else -> return -1
            }
            set?.invoke(FieldType.DAY_OF_WEAK, num)
            return 3
        }

        override fun toString(calendar: Calendar): String =
            when (calendar.dayOfWeek) {
                0 -> "Sun"
                1 -> "Mon"
                2 -> "Tue"
                3 -> "Wed"
                4 -> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                else -> throw IllegalArgumentException("Unknown day of week ${calendar.dayOfWeek}")
            }
    }

    /**
     * Day of week.
     * 1-Sun
     * 2-Mon
     * 7-Sat
     *
     * Example: "3"
     */
    object u : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text[position] == 'u'

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (text[position] !in '1'..'7') {
                return -1
            }
            set?.invoke(FieldType.DAY_OF_WEAK, text[position].toString().toInt())
            return 1
        }

        override fun toString(calendar: Calendar): String = "-"

        override fun toString(): String = "u"
    }

    /**
     * Month. Example: "Feb"
     */
    object MMM : Pattern {
        override val patternLength: Int
            get() = 3

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "MMM", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val num = when {
                text.regionMatches(position, "Jan", 0, patternLength) -> 1
                text.regionMatches(position, "Feb", 0, patternLength) -> 2
                text.regionMatches(position, "Mar", 0, patternLength) -> 3
                text.regionMatches(position, "Apr", 0, patternLength) -> 4
                text.regionMatches(position, "May", 0, patternLength) -> 5
                text.regionMatches(position, "Jun", 0, patternLength) -> 6
                text.regionMatches(position, "Jul", 0, patternLength) -> 7
                text.regionMatches(position, "Aug", 0, patternLength) -> 8
                text.regionMatches(position, "Sep", 0, patternLength) -> 9
                text.regionMatches(position, "Oct", 0, patternLength) -> 10
                text.regionMatches(position, "Nov", 0, patternLength) -> 11
                text.regionMatches(position, "Dec", 0, patternLength) -> 12
                else -> return -1
            }
            set?.invoke(FieldType.MONTH, num)
            return 3
        }

        override fun toString(calendar: Calendar): String =
            when (calendar.month) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> throw IllegalArgumentException("Unknown month ${calendar.month}")
            }

        override fun toString(): String = "MMM"
    }

    object SSS : Pattern {
        override val patternLength: Int
            get() = 3

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "SSS", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + patternLength).toIntOrNull() ?: return -1
            set?.invoke(FieldType.MILLISECOND, hours)
            return 3
        }

        override fun toString(calendar: Calendar): String =
            calendar.millisecond.as3()

        override fun toString(): String = "SSS"
    }

    object SSm : Pattern {
        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "SS", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + patternLength).toIntOrNull()?.let { it * 10 } ?: return -1
            set?.invoke(FieldType.MILLISECOND, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String {
            var r = calendar.millisecond
            while (r > 100) {
                r = r / 10
            }
            return r.as2()
        }

        override fun toString(): String = "SS"
    }

    /**
     * Timezone. Example: "-07"
     */
    object X : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "X", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val sign = text[position]
            val add = when (sign) {
                '+' -> true
                '-' -> false
                else -> return -1
            }
            if (text[position + 1] !in '0'..'9') {
                return -1
            }
            if (text[position + 2] !in '0'..'9') {
                return -1
            }
            val h = text.substring(position + 1, position + 3).toInt()
            set?.invoke(FieldType.TIME_ZONE, h * 60 * if (add) 1 else -1)
            return 3
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}"
        }
        override fun toString(): String = "X"
    }

    /**
     * Timezone. Example: "-0700"
     */
    object XX : Pattern {
        override val patternLength: Int
            get() = 2

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "XX", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 5 > text.length) {
                return -1
            }
            val sign = text[position]
            val add = when (sign) {
                '+' -> true
                '-' -> false
                else -> return -1
            }
            if (text[position + 1] !in '0'..'9') {
                return -1
            }
            if (text[position + 2] !in '0'..'9') {
                return -1
            }
            if (text[position + 3] !in '0'..'9') {
                return -1
            }
            if (text[position + 4] !in '0'..'9') {
                return -1
            }
            val h = text.substring(position + 1, position + 3).toInt()
            val m = text.substring(position + 3, position + 5).toInt()
            set?.invoke(FieldType.TIME_ZONE, h * 60 + m * if (add) 1 else -1)
            return 5
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            val m = t - h * 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}${m.as2()}"
        }
        override fun toString(): String = "XXX"
    }

    /**
     * Timezone. Example: "-07:00"
     */
    object XXX : Pattern {
        override val patternLength: Int
            get() = 3

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "XXX", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 6 > text.length) {
                return -1
            }
            val sign = text[position]
            val add = when (sign) {
                '+' -> true
                '-' -> false
                else -> return -1
            }
            if (text[position + 1] !in '0'..'9') {
                return -1
            }
            if (text[position + 2] !in '0'..'9') {
                return -1
            }
            if (text[position + 3] != ':') {
                return -1
            }
            if (text[position + 4] !in '0'..'9') {
                return -1
            }
            if (text[position + 5] !in '0'..'9') {
                return -1
            }
            val h = text.substring(position + 1, position + 3).toInt()
            val m = text.substring(position + 4, position + 6).toInt()
            set?.invoke(FieldType.TIME_ZONE, h * 60 + m * if (add) 1 else -1)
            return 6
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            val m = t - h * 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}:${m.as2()}"
        }
        override fun toString(): String = "XXX"
    }

    /**
     * Timezone. Example: "-0700"
     */
    object Z : Pattern {
        override val patternLength: Int
            get() = 3

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "Z", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int {
            if (position + 5 > text.length) {
                return -1
            }
            val sign = text[position]
            val add = when (sign) {
                '+' -> true
                '-' -> false
                else -> return -1
            }
            if (text[position + 1] !in '0'..'9') {
                return -1
            }
            if (text[position + 2] !in '0'..'9') {
                return -1
            }
            if (text[position + 3] !in '0'..'9') {
                return -1
            }
            if (text[position + 4] !in '0'..'9') {
                return -1
            }
            val h = text.substring(position + 1, position + 3).toInt()
            val m = text.substring(position + 3, position + 4).toInt()
            set?.invoke(FieldType.TIME_ZONE, h * 60 + m * if (add) 1 else -1)
            return 6
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            val m = t - h * 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}${m.as2()}"
        }
        override fun toString(): String = "Z"
    }

    /**
     * "-"
     */
    object MINUS : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "-", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            if (text[position] == '-') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = "-"
        override fun toString(): String = "-"
    }

    /**
     * ":"
     */
    object DOUBLE_POINT : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, ":", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            if (text[position] == ':') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = ":"
        override fun toString(): String = ":"
    }

    /**
     * "."
     */
    object POINT : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, ".", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            if (text[position] == '.') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = "."
        override fun toString(): String = "."
    }

    /**
     * "/"
     */
    object SLASH : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "/", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            if (text[position] == '/') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = "/"
        override fun toString(): String = "/"
    }

    /**
     * ","
     */
    object COMMA : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, ",", 0, patternLength)

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            if (text[position] == ',') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = ","
        override fun toString(): String = ","
    }

    /**
     * " "
     */
    object SPACE : Pattern {
        override val patternLength: Int
            get() = 1

        fun find(text: String, position: Int): Boolean =
            text[position] == ' '

        override fun toString(): String = " "

        override fun parse(
            text: String,
            position: Int,
            defaultTimezoneOffset: Int,
            set: ((FieldType, Int) -> Unit)?,
        ): Int =
            if (text[position] == ' ') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = " "
    }
}

internal fun Int.as2() =
    when {
        this < 10 -> "0$this"
        this >= 10 && this <= 99 -> toString()
        else -> throw IllegalArgumentException("Input integer $this should be in interval 0..99")
    }

internal fun Int.as3() =
    when {
        this < 10 -> "00$this"
        this < 100 -> "0$this"
        this < 1000 -> "$this"
        else -> throw IllegalArgumentException("Input integer $this should be in interval 0..999")
    }

internal fun Int.as4() =
    when {
        this < 10 -> "000$this"
        this < 100 -> "00$this"
        this < 1000 -> "0$this"
        this >= 1000 && this <= 9999 -> toString()
        else -> throw IllegalArgumentException("Input integer $this should be in interval 0..9999")
    }