package pw.binom.crypto

expect object ECDSASigner {
    fun sign(data: ByteArray, privateKey: ECPrivateKey): ECDSASignature
}
