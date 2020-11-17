package pw.binom.db.postgresql.async

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class NumericTest {

    @Test
    fun test() {
        val data =
            "00 02 00 00 00 00 00 02 00 7b 11 30".split(' ').map { it.toUByte(16) }.toUByteArray()
                .toByteArray()

//        assertEquals(123.44, NumericUtils.decode(data).doubleValue())

        val bb = NumericUtils.decode(
            "00 02 00 00 00 00 00 01 00 01 13 88".split(' ').map { it.toUByte(16) }.toUByteArray()
                .toByteArray()
        )

        println("->$bb   ${bb.doubleValue()}   ${BigDecimal.fromDouble(1.5, decimalMode)}")
    }
}