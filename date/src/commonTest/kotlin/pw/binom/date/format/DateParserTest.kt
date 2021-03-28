package pw.binom.date.format

import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals

class DateParserTest {
    @Test
    fun test() {
        "yyyy-MM-dd".toDatePattern().parseOrNull("1989-01-05")!!.calendar(3 * 60).apply {
            println("--->${this.toString()}  ${millisecond}")
            assertEquals(1989, year)
            assertEquals(1, month)
            assertEquals(5, dayOfMonth)
            assertEquals(0, hours)
            assertEquals(0, minutes)
            assertEquals(0, seconds)
            assertEquals(3 * 60, timeZoneOffset)
        }
        "yyyy-MM-dd HH:mm:ss.SSSXXX".toDatePattern().parseOrNull("1989-01-05 10:31:44.456+03:00")!!.calendar(3 * 60).apply {
            println("--->${this.toString()}")
            assertEquals(1989, year)
            assertEquals(1, month)
            assertEquals(5, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(31, minutes)
            assertEquals(44, seconds)
            assertEquals(456, millisecond)
            assertEquals(3 * 60, timeZoneOffset)
        }
    }
}