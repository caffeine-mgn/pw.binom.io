package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ECCurve {
    companion object {
        fun generate(params: X9ECParameters): ECCurve
    }

    fun decodePoint(data: ByteArray): EcPoint
    fun createPoint(x: BigInteger, y: BigInteger): EcPoint
    val fieldSizeInBits: Int
    val fieldSizeInBytes: Int
    val field: BigInteger
}
