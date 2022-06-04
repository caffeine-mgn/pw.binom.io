package pw.binom.crypto

import kotlinx.cinterop.COpaquePointer
import platform.openssl.sha3_Init256
import pw.binom.security.MessageDigest

actual class Keccak256MessageDigest : MessageDigest, AbstractKeccakMessageDigest() {
    override fun initContext(ctx: COpaquePointer) {
        sha3_Init256(ctx)
    }

    override val hashSize: Int
        get() = 256 / 8
}
