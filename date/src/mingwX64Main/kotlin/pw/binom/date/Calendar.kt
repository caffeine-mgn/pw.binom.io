package pw.binom.date

/*
actual class Calendar(private val utcTime: Long, actual val offset: Int) {

    private var tm_year: Int = 0
    private var tm_hour: Int = 0
    private var tm_mday: Int = 0
    private var tm_mon: Int = 0
    private var tm_min: Int = 0
    private var tm_sec: Int = 0
    private var tm_mil: Int = 0
    private var tm_wday: Int = 0

//    static void MillisToSystemTime(UINT64 millis, SYSTEMTIME *st)
//    {
//        UINT64 multiplier = 10000;
//        UINT64 t = multiplier * millis;
//
//        ULARGE_INTEGER li;
//        li.QuadPart = t;
//        // NOTE, DON'T have to do this any longer because we're putting
//        // in the 64bit UINT directly
//        //li.LowPart = static_cast<DWORD>(t & 0xFFFFFFFF);
//        //li.HighPart = static_cast<DWORD>(t >> 32);
//
//        FILETIME ft;
//        ft.dwLowDateTime = li.LowPart;
//        ft.dwHighDateTime = li.HighPart;
//
//        ::FileTimeToSystemTime(&ft, st);
//    }

    init {
        val epochDay = (utcTime / DateMath.MILLISECONDS_IN_DAY).toInt()
        println("epochDay->$epochDay")
        val time = (utcTime - (epochDay * DateMath.MILLISECONDS_IN_DAY)).absoluteValue
        println("epochDay->$epochDay    DateMath.MILLISECONDS_IN_DAY=${DateMath.MILLISECONDS_IN_DAY}")
        require(time in 0..DateMath.MILLISECONDS_IN_DAY) { "Invalid time" }
        var zeroDay = epochDay + DateMath.DAYS_0000_TO_1970
        // find the march-based year
        zeroDay -= 60 // adjust to 0000-03-01 so leap day is at end of four year cycle

        var adjust = 0
        if (zeroDay < 0) { // adjust negative years to positive for calculation
            val adjustCycles = (zeroDay + 1) / DateMath.DAYS_PER_CYCLE - 1
            adjust = adjustCycles * 400
            zeroDay += -adjustCycles * DateMath.DAYS_PER_CYCLE
        }
        var yearEst = ((400 * zeroDay.toLong() + 591) / DateMath.DAYS_PER_CYCLE).toInt()
        var doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
        if (doyEst < 0) { // fix estimate
            yearEst--
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
        }
        yearEst += adjust // reset any negative year

        val marchDoy0 = doyEst

        // convert march-based values back to january-based
        val marchMonth0 = (marchDoy0 * 5 + 2) / 153
        val month = (marchMonth0 + 2) % 12 + 1
        val dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1
        yearEst += marchMonth0 / 10

        tm_year = yearEst - 1900
        tm_mon = month
        tm_mday = dom
        println("tm_year->$tm_year")
        println("tm_mon->$tm_mon")
        println("tm_mday->$tm_mday")
        println("time->$time")
        var newtime = time
        tm_hour = (newtime / DateMath.MILLISECONDS_IN_HOUR).toInt()
        println("tm_hour->$tm_hour")
        newtime -= tm_hour * DateMath.MILLISECONDS_IN_HOUR
        tm_min = (newtime / DateMath.MILLISECONDS_IN_MINUTE).toInt()
        println("tm_min->$tm_min")
        newtime -= tm_min * DateMath.MILLISECONDS_IN_MINUTE
        tm_sec = (newtime / DateMath.MILLISECONDS_IN_SECOND).toInt()
        println("tm_sec->$tm_sec")
        newtime -= tm_sec * DateMath.MILLISECONDS_IN_SECOND
        tm_mil = newtime.toInt()
        println("tm_mil->$tm_mil")
/*
        val tm_hour = time % DateMath.MILLISECONDS_IN_HOUR
        val tm_mday = (time - (tm_hour * DateMath.MILLISECONDS_IN_HOUR)) % DateMath.MILLISECONDS_IN_MINUTE

        memScoped {
            val sysTime = alloc<_SYSTEMTIME>()
            val aa = aa(utcTime, sysTime.ptr)
            println("aa=$aa")
            val dateTime = alloc<tm>()
            val timeSec = alloc<time_tVar>()
            val tx = offset - Date.systemZoneOffset
            timeSec.value = (utcTime / 1000L + tx * 60L).convert()
            _set_errno(0)
            val vv = gmtime_s(dateTime.ptr, timeSec.ptr)
            println("vv=$vv")
            _set_errno(0)
            val timeSec2 = alloc<__time64_tVar>()
            timeSec2.value = (utcTime / 1000L + tx * 60L).convert()
            val bb = _gmtime64(timeSec2.ptr)
            println("->>>$bb $errno")

            if (localtime_r(timeSec.ptr, dateTime.ptr) == null) {
                throw IllegalArgumentException("Can't convert $utcTime to Calendar")
            }
            tm_year = dateTime.tm_year
            tm_hour = dateTime.tm_hour
            tm_mday = dateTime.tm_mday
            tm_mon = dateTime.tm_mon
            tm_min = dateTime.tm_min
            tm_sec = dateTime.tm_sec
            tm_wday = dateTime.tm_wday
        }
        */
    }

    actual val year
        get() = tm_year + 1900

    /**
     * Month, from 1 (January) to 12 (December)
     */
    actual val month
        get() = tm_mon + 1

    /**
     * Day of month, first day of month is 1
     */
    actual val dayOfMonth
        get() = tm_mday

    actual val minutes
        get() = tm_min

    actual val millisecond
        get() = tm_mil // (utcTime - utcTime / 1000L * 1000L).toInt()

    actual val hours
        get() = tm_hour

    actual val seconds
        get() = tm_sec

    actual val date
        get() = Date(utcTime)

    actual val dayOfWeek: Int
        get() = tm_wday

    actual fun timeZone(timeZoneOffset3: Int): Calendar = Calendar(utcTime = utcTime, offset = timeZoneOffset3)

    actual override fun toString(): String =
        asStringRfc822(this, timeZoneOffsetToString(offset))

    actual fun toString(timeZoneOffset4: Int): String = timeZone(timeZoneOffset4).toString()
    actual fun toDate(): Date = Date.new(this)
}
*/
