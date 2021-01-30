package pw.binom.io

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_MD
import platform.openssl.EVP_sha256
import platform.openssl.SHA256_DIGEST_LENGTH

actual class Sha256MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha256()!!

    override val finalByteArraySize: Int
        get() = SHA256_DIGEST_LENGTH
}