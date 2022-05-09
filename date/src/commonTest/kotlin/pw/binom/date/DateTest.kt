package pw.binom.date

import kotlin.math.absoluteValue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTest {
    private val currentTimeZone = test_data_currentTZ
    private val now = test_data_now

    @Test
    fun timeZone() {
        assertEquals(currentTimeZone, Date.systemZoneOffset)
    }

    @Ignore
    @Test
    fun zeroTimeTest() {
        println("--->${Date(0).calendar().iso8601()}")
        val zeroTime = Date.of(year = 0, timeZoneOffset = 0).time
        assertEquals(TestData.ZERO_TIME, zeroTime)
        println("--------->${Date.of(year = 1960, timeZoneOffset = 0).time}")
        val c = Date.of(year = 1970, timeZoneOffset = 0).calendar()
        println("c=$c")
    }

    @Ignore
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

    @Test
    fun utcTest() {
        val d = Date.of(
            year = 2021,
            month = 6,
            dayOfMonth = 10,
            hours = 14,
            minutes = 0,
            timeZoneOffset = 0,
            seconds = 0,
            millis = 0,
        )
        assertEquals(1623333600000L, d.time)
        assertEquals("Thu, 10 Jun 2021 14:00:00 GMT", d.calendar().rfc822())
    }

    @Test
    fun mskTest() {
        val d = Date.of(
            year = 2021,
            month = 6,
            dayOfMonth = 10,
            hours = 14,
            minutes = 0,
            timeZoneOffset = 180,
            seconds = 0,
            millis = 0,
        )
        assertEquals(1623322800000L, d.time)
        assertEquals("Thu, 10 Jun 2021 11:00:00 GMT", d.calendar().rfc822())
    }
}
