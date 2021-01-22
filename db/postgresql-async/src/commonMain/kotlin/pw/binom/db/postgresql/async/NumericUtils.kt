package pw.binom.db.postgresql.async

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.fromBytes
import kotlin.math.pow

//val decimalMode = DecimalMode(17, RoundingMode.FLOOR, -1)

object NumericUtils {
    fun decode(data: ByteArray): BigDecimal {
        check(data.size > 4)
//        BigDecimal.useToStringExpanded = true
        val arrayLength = Short.fromBytes(data[0], data[1])
        check(arrayLength > 1)
        var weight = Short.fromBytes(data[2], data[3]).toInt()
        val sign = Short.fromBytes(data[4], data[5])
        val dscale = Short.fromBytes(data[6], data[7]).toInt()
        var leftValue = BigInteger.ZERO
        //TODO add support of sign
//        val decimalMode = DecimalMode((dscale+2).toLong(), RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, 2)
        var i = 8
        while (weight >= 0) {
            val data = Short.fromBytes(data[i++], data[i++])
            leftValue += BigInteger.fromInt((data * 10000.0.pow(weight)).toInt())
            weight--
        }
        var totalValue = BigDecimal.fromBigInteger(leftValue)
        var g = 0
        while (i < data.size) {
            g++
            val data = Short.fromBytes(data[i++], data[i++])
            val dd = BigDecimal.fromDouble(data * 0.0001.pow(g))
            totalValue = totalValue.add(dd)
        }
//        println("Scale: ${dscale}")
//        totalValue = totalValue.scale(dscale.toLong())
        return totalValue
    }
}