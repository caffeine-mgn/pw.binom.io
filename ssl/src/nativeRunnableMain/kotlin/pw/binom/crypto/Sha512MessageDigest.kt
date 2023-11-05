package pw.binom.crypto

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.EVP_MD
import platform.openssl.EVP_sha512
import platform.openssl.SHA512_DIGEST_LENGTH
import pw.binom.security.MessageDigest

@OptIn(ExperimentalForeignApi::class)
actual class Sha512MessageDigest : MessageDigest, OpenSSLMessageDigest() {

  override fun createEvp(): CPointer<EVP_MD> = EVP_sha512()!!

  override val finalByteArraySize: Int
    get() = SHA512_DIGEST_LENGTH
}
