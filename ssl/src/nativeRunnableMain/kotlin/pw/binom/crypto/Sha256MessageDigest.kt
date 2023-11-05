package pw.binom.crypto

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.EVP_MD
import platform.openssl.EVP_sha256
import platform.openssl.SHA256_DIGEST_LENGTH
import pw.binom.security.MessageDigest

@OptIn(ExperimentalForeignApi::class)
actual class Sha256MessageDigest : MessageDigest, OpenSSLMessageDigest() {

  override fun createEvp(): CPointer<EVP_MD> = EVP_sha256()!!

  override val finalByteArraySize: Int
    get() = SHA256_DIGEST_LENGTH
}
