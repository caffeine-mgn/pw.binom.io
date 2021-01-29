package pw.binom.io

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_MD
import platform.openssl.EVP_md5

actual class MD5 : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_md5()!!

    override val finalByteArraySize: Int
        get() = 16
}