package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

expect class ECCurve {
    fun decodePoint(data: ByteArray, yBit: Boolean): EcPoint
    fun createPoint(x: BigInteger, y: BigInteger): EcPoint
}
