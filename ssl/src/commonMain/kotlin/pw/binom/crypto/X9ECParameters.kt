package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class X9ECParameters {
    val seed: ByteArray?
    val n: BigInteger
    val h: BigInteger
    val g: EcPoint
    val curve: ECCurve
}
