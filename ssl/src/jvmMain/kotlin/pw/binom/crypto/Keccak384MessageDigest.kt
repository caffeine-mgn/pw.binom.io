package pw.binom.crypto

import org.bouncycastle.jcajce.provider.digest.Keccak
import pw.binom.io.ByteBuffer
import pw.binom.security.MessageDigest

actual class Keccak384MessageDigest : MessageDigest {
    private var ctx: Keccak.Digest384? = null
    override fun init() {
        ctx = Keccak.Digest384()
    }

    private fun getCtx() = ctx ?: throw IllegalStateException("Keccak context not inited")

    override fun update(byte: Byte) {
        getCtx().update(byte)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        getCtx().update(input, offset, len)
    }

    override fun update(buffer: ByteBuffer) {
        getCtx().update(buffer.native)
    }

    override fun finish(): ByteArray {
        val ret = getCtx().digest()
        ctx = null
        return ret
    }
}
