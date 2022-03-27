package pw.binom.date.format

import pw.binom.date.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DateParserTest {
    @Test
    fun test() {
        assertEquals("2021-06-02T01:20:18.698+00:00", Date(1622596818698L).iso8601(0))

        "yyyy-MM-dd".toDatePattern().parseOrNull("1989-01-05", defaultTimezoneOffset = 3 * 60)!!.calendar(3 * 60).apply {
            assertEquals(1989, year)
            assertEquals(1, month)
            assertEquals(5, dayOfMonth)
            assertEquals(0, hours)
            assertEquals(0, minutes)
            assertEquals(0, seconds)
            assertEquals(3 * 60, offset)
        }
        "yyyy-MM-dd HH:mm:ss.SSSXXX".toDatePattern().parseOrNull("1989-01-05 10:31:44.456+03:00")!!.calendar(3 * 60)
            .apply {
                assertEquals(1989, year)
                assertEquals(1, month)
                assertEquals(5, dayOfMonth)
                assertEquals(10, hours)
                assertEquals(31, minutes)
                assertEquals(44, seconds)
                assertEquals(456, millisecond)
                assertEquals(3 * 60, offset)
            }
    }

    @Test
    fun rtc822Test() {
        val date = Date.of(
            year = 1989,
            month = 1,
            dayOfMonth = 1,
            hours = 1,
            minutes = 1,
            seconds = 1,
            millis = 0,
            timeZoneOffset = 0,
        )
        val str = date.rfc822()
        assertEquals("Sun, 01 Jan 1989 01:01:01 GMT", str)
        assertEquals(date.time, str.parseRfc822Date()!!.time)
    }

    @Test
    fun optionalTest1() {
        val pattern = "HH[:]mm".toDatePattern()
        fun assert(date: Calendar) {
            assertEquals(1, date.month)
            assertEquals(1, date.dayOfMonth)
            assertEquals(1971, date.year)
            assertEquals(14, date.hours)
            assertEquals(32, date.minutes)
            assertEquals(0, date.seconds)
            assertEquals(0, date.millisecond)
            assertEquals(Date.systemZoneOffset, date.offset)
        }
        assert(pattern.parseOrNull("14:32")!!.calendar())
        assert(pattern.parseOrNull("1432")!!.calendar())
    }

    @Test
    fun optionalTest2() {
        val pattern = "[yyyy[-]MM ]HHmm".toDatePattern()
        fun assert(date: Calendar) {
            assertEquals(1, date.month)
            assertEquals(1, date.dayOfMonth)
            assertEquals(1971, date.year)
            assertEquals(14, date.hours)
            assertEquals(32, date.minutes)
            assertEquals(0, date.seconds)
            assertEquals(0, date.millisecond)
            assertEquals(Date.systemZoneOffset, date.offset)
        }
        assert(pattern.parseOrNull("1432")!!.calendar())
        assert(pattern.parseOrNull("197101 1432")!!.calendar())
        assert(pattern.parseOrNull("1971-01 1432")!!.calendar())
    }

    @Test
    fun optionalTest3(){
        val pattern = "yyyy[-]".toDatePattern()

        fun assert(date: Calendar) {
            assertEquals(1, date.month)
            assertEquals(1, date.dayOfMonth)
            assertEquals(2021, date.year)
            assertEquals(0, date.hours)
            assertEquals(0, date.minutes)
            assertEquals(0, date.seconds)
            assertEquals(0, date.millisecond)
            assertEquals(Date.systemZoneOffset, date.offset)
        }
        assert(pattern.parseOrNull("2021")!!.calendar())
        assert(pattern.parseOrNull("2021-")!!.calendar())
    }

    @Test
    fun orTest() {
        val pattern = "yyyy[(-|.|/| )]MM[(-|.|/| )]dd".toDatePattern()

        fun assert(date: Calendar) {
            assertEquals(8, date.month)
            assertEquals(2, date.dayOfMonth)
            assertEquals(2021, date.year)
        }
        assert(pattern.parseOrNull("2021-08-02")!!.calendar())
        assert(pattern.parseOrNull("2021.08.02")!!.calendar())
        assert(pattern.parseOrNull("2021 08.02")!!.calendar())
        assert(pattern.parseOrNull("202108.02")!!.calendar())
        assert(pattern.parseOrNull("20210802")!!.calendar())
    }
}