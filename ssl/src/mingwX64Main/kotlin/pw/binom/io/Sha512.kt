package pw.binom.io

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_MD
import platform.openssl.EVP_sha512
import platform.openssl.SHA512_DIGEST_LENGTH

actual class Sha512 : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha512()!!

    override val finalByteArraySize: Int
        get() = SHA512_DIGEST_LENGTH
}