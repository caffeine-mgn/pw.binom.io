package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

expect class ECCurve {
    companion object {
        fun generate(params: X9ECParameters): ECCurve
    }

    fun decodePoint(data: ByteArray, yBit: Boolean): EcPoint
    fun createPoint(x: BigInteger, y: BigInteger): EcPoint
}
