package pw.binom.db.postgresql.async

import kotlin.test.Test
import kotlin.test.assertEquals

class NumericTest {

    @Test
    fun test() {
        val data1 =
            "00 02 00 00 00 00 00 02 00 7b 11 30".split(' ').map { it.toUByte(16) }.toUByteArray()
                .toByteArray()

        assertEquals(123.44, NumericUtils.decode(data1).toStringExpanded().toDouble())

        val data2 =
            "00 02 00 00 00 00 00 01 00 01 13 88".split(' ').map { it.toUByte(16) }.toUByteArray()
                .toByteArray()

        assertEquals(1.5, NumericUtils.decode(data2).toString().toDouble())
    }
}
