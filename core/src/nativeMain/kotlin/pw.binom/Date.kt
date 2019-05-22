package pw.binom

actual class Date actual constructor(actual val time: Long) {

    private val native = getNativeTime(time)

    actual val year: Int
        get() = native.tm_year

    actual val month: Int
        get() = native.tm_mon

    actual val dayOfMonth: Int
        get() = native.tm_mday

    actual val hours: Int
        get() = native.tm_hour

    actual val min: Int
        get() = native.tm_min

    actual val sec: Int
        get() = native.tm_sec

    actual val dayOfWeek: Int
        get() = native.tm_wday

    actual constructor(year: Int, month: Int, dayOfMonth: Int, hours: Int, min: Int, sec: Int) :
            this(calcTime(year = year, month = month, dayOfMonth = dayOfMonth, hours = hours, min = min, sec = sec))


    actual companion object {
        actual fun now(): Date = Date(nowTime())
        actual val timeZoneOffset: Int
            get() = currentTimezoneOffset()
    }

}