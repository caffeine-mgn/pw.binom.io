package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

class RSAPrivateKeySpec(
    val modulus: BigInteger,
    val privateExponent: BigInteger
) : KeySpec
