package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.Key

expect class RSAPrivateKey : Key.Private {
    companion object {
        fun load(encodedKeySpec: KeySpec): RSAPrivateKey
    }

    val n: BigInteger
    val e: BigInteger
    val d: BigInteger
}
