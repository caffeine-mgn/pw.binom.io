package pw.binom.crypto

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.EVP_MD
import platform.openssl.EVP_sha1
import pw.binom.security.MessageDigest

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Sha1MessageDigest : MessageDigest, OpenSSLMessageDigest() {

  override fun createEvp(): CPointer<EVP_MD> = EVP_sha1()!!

  override val finalByteArraySize: Int
    get() = 20
}
