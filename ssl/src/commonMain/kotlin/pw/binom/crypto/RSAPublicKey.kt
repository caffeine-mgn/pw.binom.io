package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.Key

expect class RSAPublicKey : Key.Public {
    val e: BigInteger
    val n: BigInteger
}
