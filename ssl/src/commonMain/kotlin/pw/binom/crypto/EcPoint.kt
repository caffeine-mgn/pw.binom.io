package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class EcPoint {
    val x: BigInteger
    val y: BigInteger
    val curve: ECCurve
    val isInfinity: Boolean
    fun multiply(k: BigInteger): EcPoint
    fun add(k: EcPoint): EcPoint
    fun getEncoded(compressed: Boolean): ByteArray
}
