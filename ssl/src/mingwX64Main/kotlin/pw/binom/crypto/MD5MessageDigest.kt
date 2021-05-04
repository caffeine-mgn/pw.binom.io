package pw.binom.crypto

actual class MD5MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_md5()!!

    override val finalByteArraySize: Int
        get() = 16
}