package pw.binom.date

import pw.binom.date.format.toDatePattern
import kotlin.test.Test
import kotlin.test.assertEquals

class IsoParserTest {

    @Test
    fun test() {
        /*
        "2021-03-29".parseIso8601Date(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(0, hours)
            assertEquals(0, minutes)
            assertEquals(0, seconds)
            assertEquals(0, timeZoneOffset)
        }
        "2021-03-29 10:17:33".parseIso8601Date(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(0, timeZoneOffset)
        }

        "2021-03-29 10:17:33+03:00".parseIso8601Date(0)!!.calendar(60 * 3).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(60 * 3, timeZoneOffset)
        }

        "2021-03-29 10:17:33+00:00".parseIso8601Date(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(0, timeZoneOffset)
        }

        "2021-03-29T10:17:33+00:00".parseIso8601Date(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(0, timeZoneOffset)
        }

        "2021-03-29 10:17:33.37+00".parseIso8601Date(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(370, millisecond)
            assertEquals(0, timeZoneOffset)
        }
        */
        "2021-03-29T10:17:33.039440139Z".parseIso8601Date(0)!!.calendar(0).apply {
            assertEquals(2021, year)
            assertEquals(3, month)
            assertEquals(29, dayOfMonth)
            assertEquals(10, hours)
            assertEquals(17, minutes)
            assertEquals(33, seconds)
            assertEquals(39, millisecond)
            assertEquals(0, timeZoneOffset)
        }
    }
}