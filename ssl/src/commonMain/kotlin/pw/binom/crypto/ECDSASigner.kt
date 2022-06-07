package pw.binom.crypto

expect class ECDSASigner {
    constructor(privateKey: ECPrivateKey)

    fun sign(data: ByteArray): ECDSASignature
}
