package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.MessageDigest

actual class Sha512MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha512()!!

    override val finalByteArraySize: Int
        get() = SHA512_DIGEST_LENGTH
}