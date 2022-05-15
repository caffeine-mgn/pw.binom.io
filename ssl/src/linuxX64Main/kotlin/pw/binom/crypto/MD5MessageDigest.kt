package pw.binom.crypto

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_MD
import platform.openssl.EVP_md5
import pw.binom.security.MessageDigest

actual class MD5MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_md5()!!

    override val finalByteArraySize: Int
        get() = 16
}
