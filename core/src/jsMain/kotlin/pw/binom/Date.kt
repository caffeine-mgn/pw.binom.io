package pw.binom

/*
actual class Date {

    private val obj: kotlin.js.Date

    actual constructor(time: Long) {
        obj = kotlin.js.Date(time)
    }

    internal actual constructor(year: Int, month: Int, dayOfMonth: Int, hours: Int, min: Int, sec: Int) {
        obj = kotlin.js.Date(year, month, dayOfMonth, hours, min, sec)
    }

    actual val year: Int
        get() = obj.getFullYear() - 1900
    actual val month: Int
        get() = obj.getMonth()
    actual val dayOfMonth: Int
        get() = obj.getDate()
    actual val dayOfWeek: Int
        get() = obj.getDay()
    actual val hours: Int
        get() = obj.getHours()
    actual val min: Int
        get() = obj.getMinutes()
    actual val sec: Int
        get() = obj.getSeconds()
    actual val time: Long
        get() = obj.getTime().toLong()

    actual companion object {
        */
/**
         * Returns current time
         *//*

        actual fun now(): Date = Date(js("new Date().getTime()"))

        private val offset: Int = js("new Date().getTimezoneOffset()")

        */
/**
         * Returns current offset UTC in minutes
         *//*

        actual val timeZoneOffset: Int
            get() = offset

    }

}*/
