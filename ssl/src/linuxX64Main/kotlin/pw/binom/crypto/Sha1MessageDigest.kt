package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.MessageDigest

actual class Sha1MessageDigest : MessageDigest, OpenSSLMessageDigest() {

    override fun createEvp(): CPointer<EVP_MD> = EVP_sha1()!!

    override val finalByteArraySize: Int
        get() = 20
}