package pw.binom.crypto

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.security.MessageDigest

actual class Sha3MessageDigest actual constructor(val size: Size) : MessageDigest, OpenSSLMessageDigest() {
    actual enum class Size {
        S224,
        S254,
        S256,
        S384,
        S512,
    }

    override fun createEvp(): CPointer<EVP_MD> = when (size) {
        Size.S224 -> EVP_sha3_224()!!
        Size.S254 -> EVP_sha3_224()!!
        Size.S256 -> EVP_sha3_256()!!
        Size.S384 -> EVP_sha3_384()!!
        Size.S512 -> EVP_sha3_512()!!
    }

    override val finalByteArraySize: Int
        get() = 20
}
