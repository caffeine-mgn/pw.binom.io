package pw.binom.date.format

import pw.binom.date.Calendar
import kotlin.math.absoluteValue

sealed class Pattern {
    companion object {
        fun find(format: String, position: Int): Pattern? =
            when {
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
                Z.find(format, position) -> Z
                MINUS.find(format, position) -> MINUS
                SLASH.find(format, position) -> SLASH
                DOUBLE_POINT.find(format, position) -> DOUBLE_POINT
                POINT.find(format, position) -> POINT
                SPACE.find(format, position) -> SPACE
                else -> null
            }
    }

    abstract fun find(text: String, position: Int): Boolean
    abstract val len: Int
    open fun len(text: String, position: Int): Int = if (find(text, position)) len else -1
    abstract fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int
    abstract fun toString(calendar: Calendar): String

    enum class FieldType {
        YEAR, MONTH, DAY_OF_MONTH, DAY_OF_WEAK, HOURS, MINUTES, SECONDS, MILLISECOND, TIME_ZONE
    }

    /**
     * Year. Example: "2021"
     */
    object yyyy : Pattern() {
        override fun find(text: String, position: Int) =
            text.regionMatches(position, "yyyy", 0, len)

        override val len: Int
            get() = 4

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 4 > text.length) {
                return -1
            }
            val year = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.YEAR, year)
            return 4
        }

        override fun toString(calendar: Calendar): String = calendar.year.as4()
    }

    /**
     * Month. Example: "04"
     */
    object MM : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "MM", 0, len)

        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val year = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.MONTH, year)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.month.as2()
    }

    /**
     * Day of month. Example: "15"
     */
    object dd : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "dd", 0, len)

        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val year = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.DAY_OF_MONTH, year)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.dayOfMonth.as2()
    }

    class CustomText(pattern: String, val start: Int) : Pattern() {
        companion object {
            fun find(text: String, position: Int): Boolean =
                text[position] == '\'' && (text.indexOf("'", position + 1) != -1)
        }

        override fun find(text: String, position: Int): Boolean =
            text[position] == '\'' && (text.indexOf("'", position + 1) != -1)

        override val len: Int = pattern.indexOf("'", start + 1) - start + 1
        val text = pattern.substring(start + 1, start + len - 1)

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            return this.text.length
//            text.regionMatches(position, text, 0, text.length)
//            TODO("Not yet implemented")
        }

        override fun toString(calendar: Calendar): String =
            text

//        override fun len(text: String, position: Int): Int {
//            val end = text.indexOf("'", position + 1)
//            return end - position + 1
//        }
    }

    /**
     * Hours. Example: "31"
     */
    object HH : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "HH", 0, len)

        override fun len(text: String, position: Int): Int = 2
        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.HOURS, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.hours.as2()
    }

    /**
     * Minutes. Example: "31"
     */
    object mm_ : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "mm", 0, len)

        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.MINUTES, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.minutes.as2()
    }

    //Seconds. Example: "31"
    object ss : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "ss", 0, len)

        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.SECONDS, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String = calendar.seconds.as2()
    }

    //Day of week. Example: "Mon"
    object EEE : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "EEE", 0, len)

        override val len: Int
            get() = 3

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val num = when {
                text.regionMatches(position, "Sun", 0, len) -> 0
                text.regionMatches(position, "Mon", 0, len) -> 1
                text.regionMatches(position, "Tue", 0, len) -> 2
                text.regionMatches(position, "Wed", 0, len) -> 3
                text.regionMatches(position, "Thu", 0, len) -> 4
                text.regionMatches(position, "Fri", 0, len) -> 5
                text.regionMatches(position, "Sat", 0, len) -> 6
                else -> return -1
            }
            set(FieldType.DAY_OF_WEAK, num)
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
    object u : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text[position] == 'u'

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (text[position] !in '1'..'7') {
                return -1
            }
            set(FieldType.DAY_OF_MONTH, text[position].toString().toInt())
            return 1
        }

        override fun toString(calendar: Calendar): String = "-"
    }

    /**
     * Month. Example: "Feb"
     */
    object MMM : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "MMM", 0, len)

        override val len: Int
            get() = 3

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val num = when {
                text.regionMatches(position, "Jan", 0, len) -> 1
                text.regionMatches(position, "Feb", 0, len) -> 2
                text.regionMatches(position, "Mar", 0, len) -> 3
                text.regionMatches(position, "Apr", 0, len) -> 4
                text.regionMatches(position, "May", 0, len) -> 5
                text.regionMatches(position, "Jun", 0, len) -> 6
                text.regionMatches(position, "Jul", 0, len) -> 7
                text.regionMatches(position, "Aug", 0, len) -> 8
                text.regionMatches(position, "Sep", 0, len) -> 9
                text.regionMatches(position, "Oct", 0, len) -> 10
                text.regionMatches(position, "Nov", 0, len) -> 11
                text.regionMatches(position, "Dec", 0, len) -> 12
                else -> return -1
            }
            set(FieldType.MONTH, num)
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
    }

    object SSS : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "SSS", 0, len)

        override val len: Int
            get() = 3

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 3 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + len).toIntOrNull() ?: return -1
            set(FieldType.MILLISECOND, hours)
            return 3
        }

        override fun toString(calendar: Calendar): String =
            calendar.millisecond.as3()
    }

    object SSm : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "SS", 0, len)

        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
            if (position + 2 > text.length) {
                return -1
            }
            val hours = text.substring(position, position + len).toIntOrNull()?.let { it * 10 } ?: return -1
            set(FieldType.MILLISECOND, hours)
            return 2
        }

        override fun toString(calendar: Calendar): String =
            calendar.millisecond.as2()
    }

    /**
     * Timezone. Example: "-07:00"
     */
    object XX : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "XX", 0, len)

        override val len: Int
            get() = 2

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
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
            set(FieldType.TIME_ZONE, h * 60 * if (add) 1 else -1)
            return 6
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            val m = t - h * 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}:${m.as2()}"
        }
    }

    /**
     * Timezone. Example: "-07:00"
     */
    object XXX : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "XXX", 0, len)

        override val len: Int
            get() = 3

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
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
            set(FieldType.TIME_ZONE, h * 60 + m * if (add) 1 else -1)
            return 6
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            val m = t - h * 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}:${m.as2()}"
        }
    }

    /**
     * Timezone. Example: "-0700"
     */
    object Z : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "XXX", 0, len)

        override val len: Int
            get() = 3

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int {
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
            set(FieldType.TIME_ZONE, h * 60 + m * if (add) 1 else -1)
            return 6
        }

        override fun toString(calendar: Calendar): String {
            val t = calendar.timeZoneOffset.absoluteValue
            val h = t / 60
            val m = t - h * 60
            return "${if (calendar.timeZoneOffset >= 0) '+' else '-'}${h.as2()}${m.as2()}"
        }
    }

    /**
     * "-"
     */
    object MINUS : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "-", 0, len)

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int =
            if (text[position] == '-') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = "-"
    }

    /**
     * ":"
     */
    object DOUBLE_POINT : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, ":", 0, len)

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int =
            if (text[position] == ':') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = ":"
    }

    /**
     * "."
     */
    object POINT : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, ".", 0, len)

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int =
            if (text[position] == '.') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = "."
    }

    /**
     * "/"
     */
    object SLASH : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, "/", 0, len)

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int =
            if (text[position] == '/') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = "/"
    }

    /**
     * ","
     */
    object COMMA : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text.regionMatches(position, ",", 0, len)

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int =
            if (text[position] == ',') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = ","
    }

    /**
     * " "
     */
    object SPACE : Pattern() {
        override fun find(text: String, position: Int): Boolean =
            text[position] == ' '

        override val len: Int
            get() = 1

        override fun parse(text: String, position: Int, set: (FieldType, Int) -> Unit): Int =
            if (text[position] == ' ') {
                1
            } else {
                -1
            }

        override fun toString(calendar: Calendar): String = " "
    }
}

private fun Int.as2() =
    when {
        this < 10 -> "0$this"
        this >= 10 && this <= 99 -> toString()
        else -> throw IllegalArgumentException("Input integer $this should be in interval 0..99")
    }

private fun Int.as3() =
    when {
        this < 10 -> "00$this"
        this < 100 -> "0$this"
        this < 1000 -> "$this"
        else -> throw IllegalArgumentException("Input integer $this should be in interval 0..999")
    }

private fun Int.as4() =
    when {
        this < 10 -> "000$this"
        this < 100 -> "00$this"
        this < 1000 -> "0$this"
        this >= 1000 && this <= 9999 -> toString()
        else -> throw IllegalArgumentException("Input integer $this should be in interval 0..9999")
    }