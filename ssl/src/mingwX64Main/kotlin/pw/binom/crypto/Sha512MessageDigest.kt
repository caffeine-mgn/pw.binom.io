package pw.binom.crypto

actual class Sha512MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha512()!!

    override val finalByteArraySize: Int
        get() = SHA512_DIGEST_LENGTH
}