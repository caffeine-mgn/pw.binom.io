package pw.binom.crypto

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_MD
import platform.openssl.EVP_sha1
import pw.binom.security.MessageDigest

actual class Sha1MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha1()!!

    override val finalByteArraySize: Int
        get() = 20
}
