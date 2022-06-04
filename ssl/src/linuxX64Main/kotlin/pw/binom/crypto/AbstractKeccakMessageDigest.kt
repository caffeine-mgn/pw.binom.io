package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.ByteBuffer
import pw.binom.security.MessageDigest

abstract class AbstractKeccakMessageDigest : MessageDigest {
    protected val ctx = ByteArray(sizeOf<sha3_context>().toInt())
    protected abstract fun initContext(ctx: COpaquePointer)
    protected abstract val hashSize: Int

    override fun init() {
        ctx.usePinned { p ->
            initContext(p.addressOf(0))
            sha3_SetFlags(p.addressOf(0), SHA3_FLAGS_KECCAK)
        }
    }

    override fun update(byte: Byte) {
        memScoped {
            ctx.usePinned { b ->
                val bb = alloc<ByteVar>()
                bb.value = byte
                sha3_Update(b.addressOf(0), bb.ptr, 1.convert())
            }
        }
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        if (len == 0) {
            return
        }
        input.usePinned { i ->
            ctx.usePinned { b ->
                sha3_Update(b.addressOf(0), i.addressOf(offset), len.convert())
            }
        }
    }

    override fun update(buffer: ByteBuffer) {
        if (buffer.remaining <= 0) {
            return
        }
        ctx.usePinned { b ->
            buffer.ref { cPointer, size ->
                sha3_Update(b.addressOf(0), cPointer, size.convert())
            }
        }
    }

    override fun finish(): ByteArray = ctx.usePinned { b ->
        sha3_Finalize(b.addressOf(0))!!.readBytes(hashSize)
    }
}
