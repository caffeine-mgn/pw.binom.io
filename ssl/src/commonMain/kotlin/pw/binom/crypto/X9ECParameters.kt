package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

expect class X9ECParameters {
    val seed: ByteArray?
    val n: BigInteger
    val h: BigInteger
    val g: EcPoint
    val curve: ECCurve
}
