package pw.binom.crypto

actual class Sha1MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha1()!!

    override val finalByteArraySize: Int
        get() = 20
}