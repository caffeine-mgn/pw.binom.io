package pw.binom.crypto

actual class Sha256MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha256()!!

    override val finalByteArraySize: Int
        get() = SHA256_DIGEST_LENGTH
}