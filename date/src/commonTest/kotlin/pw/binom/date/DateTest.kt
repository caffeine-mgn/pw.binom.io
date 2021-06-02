package pw.binom.date

import pw.binom.Environment
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.workDirectory
import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTest {
    private val testDate = File(File(Environment.workDirectory), "build/tmp-date/")
    private val currentTimeZone = File(testDate, "currentTZ").read().utf8Reader().use {
        it.readText()
    }.toInt()

    private val now = File(testDate, "now").read().utf8Reader().use {
        it.readText()
    }.toLong()

    @Test
    fun timeZone() {
        assertEquals(currentTimeZone, Date.timeZoneOffset)
    }

    @Test
    fun nowTest() {
        val except = now
        val actual = Date.nowTime
        assertTrue("except: [$except], actual: [$actual]") {
            (except - actual).absoluteValue < 10_000
        }
    }

    @Test
    fun ofTest() {
        val date = Date.of(
                year = 1989,
                month = 1,
                dayOfMonth = 5,
                hours = 13,
                minutes = 0,
                seconds = 0,
                millis = 0,
                timeZoneOffset = 0
        )
        assertEquals(600008400000L, date.time)
    }

    @Test
    fun calendarTest() {
        val date = Date.of(
                year = 1989,
                month = 1,
                dayOfMonth = 5,
                hours = 13,
                minutes = 0,
                seconds = 0,
                millis = 0,
                timeZoneOffset = 0
        ).calendar(0)

        assertEquals(1989, date.year, "year")
        assertEquals(1, date.month, "month")
        assertEquals(5, date.dayOfMonth, "dayOfMonth")
        assertEquals(13, date.hours, "hour")
        assertEquals(0, date.seconds, "second")
        assertEquals(0, date.millisecond, "millisecond")
    }
}