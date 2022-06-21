package pw.binom.date

/*
actual class Calendar(private val utcTime: Long, actual val offset: Int) {

    private val tt = memScoped {
        val t = alloc<time_tVar>()
        val tx = offset - Date.systemZoneOffset
        t.value = (utcTime / 1000L + tx * 60L).convert()
        localtime(t.ptr)!!.pointed
    }

    actual val year
        get() = tt.tm_year + 1900

    /**
     * Month, from 1 (January) to 12 (December)
     */
    actual val month
        get() = tt.tm_mon + 1

    /**
     * Day of month, first day of month is 1
     */
    actual val dayOfMonth
        get() = tt.tm_mday

    actual val minutes
        get() = tt.tm_min

    actual val millisecond
        get() = (utcTime - utcTime / 1000L * 1000L).toInt()

    actual val hours
        get() = tt.tm_hour

    actual val seconds
        get() = tt.tm_sec

    actual val dayOfWeek: Int
        get() = tt.tm_wday

    actual val date
        get() = Date(utcTime)

    actual fun timeZone(timeZoneOffset3: Int): Calendar = Calendar(utcTime = utcTime, offset = timeZoneOffset3)

    actual override fun toString(): String =
        asStringRfc822(this, timeZoneOffsetToString(offset))

    actual fun toString(timeZoneOffset4: Int): String = timeZone(timeZoneOffset4).toString()

    actual fun toDate(): Date = Date.new(this)
}
*/
