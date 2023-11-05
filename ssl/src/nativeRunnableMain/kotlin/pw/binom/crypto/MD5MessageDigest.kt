package pw.binom.crypto

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.EVP_MD
import platform.openssl.EVP_md5
import pw.binom.security.MessageDigest

@OptIn(ExperimentalForeignApi::class)
actual class MD5MessageDigest : MessageDigest, OpenSSLMessageDigest() {

  actual companion object;

  override fun createEvp(): CPointer<EVP_MD> = EVP_md5()!!

  override val finalByteArraySize: Int
    get() = 16
}
