package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.ECKey
import pw.binom.ssl.Key

expect class ECPrivateKey : Key.Private, ECKey {
    val d: BigInteger

    companion object {
        fun load(data: ByteArray): ECPrivateKey
    }
}
