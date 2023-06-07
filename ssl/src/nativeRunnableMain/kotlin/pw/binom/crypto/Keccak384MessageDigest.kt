package pw.binom.crypto

import kotlinx.cinterop.COpaquePointer
import platform.openssl.sha3_Init384
import pw.binom.security.MessageDigest

actual class Keccak384MessageDigest : MessageDigest, AbstractKeccakMessageDigest() {
    override fun initContext(ctx: COpaquePointer) {
        sha3_Init384(ctx)
    }

    override val hashSize: Int
        get() = 384 / 8
}
