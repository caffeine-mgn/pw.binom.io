package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

expect class EcPoint {
    val x: BigInteger
    val y: BigInteger
    val curve: ECCurve
    fun multiply(k: BigInteger): EcPoint
    fun getEncoded(compressed: Boolean): ByteArray
}
