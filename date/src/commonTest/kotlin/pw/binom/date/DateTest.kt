package pw.binom.date

import pw.binom.io.file.File
import pw.binom.io.file.inputStream
import pw.binom.io.file.workDirectory
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Reader
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTest {
    private val testDate = File(File.workDirectory, "build/tmp-date/")
    private val currentTimeZone = File(testDate, "currentTZ").inputStream!!.use {
        it.utf8Reader().readText()
    }.toInt()

    private val now = File(testDate, "now").inputStream!!.use {
        it.utf8Reader().readText()
    }.toLong()

    @Test
    fun timeZone() {
        assertEquals(currentTimeZone, Date.timeZoneOffset)
    }

    @Test
    fun nowTest() {
        val except = now
        val actual = Date.now
        assertTrue("except: [$except], actual: [$actual]") {
            abs(except - actual) < 10_000
        }
    }

    @Test
    fun ofTest() {
        val date = Date.of(
                year = 1989,
                month = 0,
                dayOfMonth = 5,
                hours = 13,
                minutes = 0,
                seconds = 0,
                millis = 0,
                timeZoneOffset = Date.timeZoneOffset
        )
        assertEquals(599997600000L, date.time)
    }

    @Test
    fun calendarTest() {
        val date = Date.of(
                year = 1989,
                month = 0,
                dayOfMonth = 5,
                hours = 13,
                minutes = 0,
                seconds = 0,
                millis = 0,
                timeZoneOffset = 0
        ).calendar(0)

        assertEquals(1989, date.year, "year")
        assertEquals(0, date.month, "month")
        assertEquals(5, date.dayOfMonth, "dayOfMonth")
        assertEquals(13, date.hours, "hour")
        assertEquals(0, date.seconds, "second")
        assertEquals(0, date.millisecond, "millisecond")
    }
}