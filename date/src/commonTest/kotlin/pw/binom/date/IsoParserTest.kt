package pw.binom.date

import pw.binom.date.format.toDatePattern
import kotlin.test.Test
import kotlin.test.assertEquals

class IsoParserTest {

    @Test
    fun test() {
        println("->${Date.timeZoneOffset}")
        "2021-03-29".parseIsoDate(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(0, hours)
            assertEquals(0, minutes)
            assertEquals(0, seconds)
            assertEquals(0, timeZoneOffset)
        }
        "2021-03-29 10:17:33".parseIsoDate(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(0, timeZoneOffset)
        }

        "2021-03-29 10:17:33+03:00".parseIsoDate(0)!!.calendar(60*3).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(60*3, timeZoneOffset)
        }

        "2021-03-29 10:17:33+00:00".parseIsoDate(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(0, timeZoneOffset)
        }
    }
}