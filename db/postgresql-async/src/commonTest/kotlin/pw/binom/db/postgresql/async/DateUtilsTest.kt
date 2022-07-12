package pw.binom.db.postgresql.async

import kotlin.test.Test

class DateUtilsTest {
    @Test
    fun parseDateTest() {
        DateUtils.parseDate("0001-12-11 12:00:00 BC")
    }
}
