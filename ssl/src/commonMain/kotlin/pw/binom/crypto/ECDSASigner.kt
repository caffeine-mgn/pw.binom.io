package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger

expect class ECDSASigner {
    constructor(privateKey: ECPrivateKey)
    constructor(publicKey: ECPublicKey)

    fun sign(data: ByteArray): ECDSASignature
    fun verify(data: ByteArray, r: BigInteger, s: BigInteger): Boolean
    fun verify(data: ByteArray, signature: ECDSASignature): Boolean
}
