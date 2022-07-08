package pw.binom.date

actual class Calendar(private val utcTime: Long, actual val offset: Int) {

    actual val year: Int
    actual val month: Int
    actual val dayOfMonth: Int
    actual val hours: Int
    actual val minutes: Int
    actual val seconds: Int
    actual val millisecond: Int
    actual val dayOfWeek: Int

    init {
        val utcTime = utcTime + offset * MILLISECONDS_IN_MINUTE
        var epochDay = (utcTime / MILLISECONDS_IN_DAY).toInt()
        var time = utcTime % MILLISECONDS_IN_DAY // (utcTime - (epochDay * DateMath.MILLISECONDS_IN_DAY))

        if (time < 0) {
            time = MILLISECONDS_IN_DAY + time
            epochDay -= 1
        }
        require(time in 0..MILLISECONDS_IN_DAY) { "Invalid time" }
        var zeroDay = epochDay + DAYS_0000_TO_1970
        // find the march-based year
        zeroDay -= 60 // adjust to 0000-03-01 so leap day is at end of four year cycle

        var adjust = 0
        if (zeroDay < 0) { // adjust negative years to positive for calculation
            val adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1
            adjust = adjustCycles * 400
            zeroDay += -adjustCycles * DAYS_PER_CYCLE
        }
        var yearEst = ((400 * zeroDay.toLong() + 591) / DAYS_PER_CYCLE).toInt()
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

        year = yearEst
        this.month = month
        dayOfMonth = dom
        var newtime = time
        hours = (newtime / MILLISECONDS_IN_HOUR).toInt()
        newtime -= hours * MILLISECONDS_IN_HOUR
        minutes = (newtime / MILLISECONDS_IN_MINUTE).toInt()
        newtime -= minutes * MILLISECONDS_IN_MINUTE
        seconds = (newtime / MILLISECONDS_IN_SECOND).toInt()
        newtime -= seconds * MILLISECONDS_IN_SECOND
        millisecond = newtime.toInt()
        dayOfWeek = when (val dw = (epochDay + 3).mod(7)) {
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 0
            else -> TODO("Invalid day of week $dw")
        }
    }

    actual val dateTime: DateTime
        get() = DateTime(utcTime)

    /**
     * @param timeZoneOffset4 TimeZone offset in mintes
     */
    actual fun toString(timeZoneOffset4: Int): String = if (offset == timeZoneOffset4) {
        toString()
    } else {
        timeZone(timeZoneOffset4).toString()
    }

    actual override fun toString(): String = iso8601()

    /**
     * Changes current TimeZone.
     *
     * @param timeZoneOffset3 TimeZone offset in mintes
     */
    actual fun timeZone(timeZoneOffset3: Int): Calendar = Calendar(utcTime = utcTime, offset = timeZoneOffset3)

    actual fun toDate(): DateTime = dateTime
}
