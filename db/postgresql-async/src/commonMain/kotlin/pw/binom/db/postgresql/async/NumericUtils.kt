package pw.binom.db.postgresql.async

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.fromBytes
import kotlin.math.pow

val decimalMode = DecimalMode(17, RoundingMode.FLOOR, -1)

object NumericUtils {
    fun decode(data: ByteArray): BigDecimal {
        BigDecimal.useToStringExpanded = true
        val arrayLength = Short.fromBytes(data[0], data[1])
        var weight = Short.fromBytes(data[2], data[3]).toInt()
        val sign = Short.fromBytes(data[4], data[5])
        val dscale = Short.fromBytes(data[6], data[7]).toInt()
        var i = 8
        var t1 = BigInteger.ZERO
//        val decimalMode = DecimalMode((dscale-1).toLong(), RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, -1)
        while (weight >= 0) {
            val d = Short.fromBytes(data[i++], data[i++])
            t1 += BigInteger.fromInt((d * 10000.0.pow(weight)).toInt())
            weight--
        }
        var t3 = BigDecimal.fromBigInteger(t1, decimalMode)
        var g = 0
        while (i < data.size) {
            g++
            val d = Short.fromBytes(data[i++], data[i++])
            val bb = (d * 0.0001.pow(g))
            val dd = BigDecimal.fromDouble(bb)
            t3 = t3.add(dd, decimalMode)
        }
//        t3 = t3.scale(dscale.toLong())
        return t3
    }
}