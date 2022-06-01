package pw.binom.crypto

class PKCS8EncodedKeySpec(override val data: ByteArray) : EncodedKeySpec {
    override val format: String
        get() = "PKCS#8"
}
