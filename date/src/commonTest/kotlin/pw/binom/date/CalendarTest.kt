package pw.binom.date

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarTest {

    @Test
    fun monthTest() {
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

        val calc = date.calendar(0)
        assertEquals(1, calc.month)
        assertEquals("Thu, 05 Jan 1989 13:00:00 GMT", calc.rfc822())
    }
}