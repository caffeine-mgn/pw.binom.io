package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.ByteBuffer
import pw.binom.security.MessageDigest

abstract class OpenSSLMessageDigest : MessageDigest {
    private var ptr: CPointer<EVP_MD_CTX>? = null
    protected abstract fun createEvp(): CPointer<EVP_MD>
    protected abstract val finalByteArraySize: Int
    override fun init() {
        if (ptr != null) {
            EVP_MD_CTX_free(ptr)
            ptr = null
        }
        checkInit()
    }

    private fun checkInit() {
        if (ptr == null) {
            ptr = EVP_MD_CTX_new()
            EVP_DigestInit_ex(ptr, createEvp(), null)
        }
    }

    override fun update(byte: Byte) {
        checkInit()
        memScoped {
            val ar = allocArray<UByteVar>(1)
            ar[0] = byte.toUByte()
            EVP_DigestUpdate(ptr, ar, 1.convert())
        }
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        checkInit()
        EVP_DigestUpdate(ptr, input.refTo(offset), len.convert())
    }

    override fun update(buffer: ByteBuffer) {
        checkInit()
        buffer.ref { bufferPtr, remaining ->
            EVP_DigestUpdate(ptr, bufferPtr, remaining.convert())
        }
        buffer.position = buffer.limit
    }

    override fun finish(): ByteArray {
        checkInit()
        val out = ByteArray(finalByteArraySize)
        memScoped {
            val size = alloc<UIntVar>()
            EVP_DigestFinal(ptr, out.refTo(0).getPointer(this).reinterpret(), size.ptr)
            if (size.value != out.size.convert<UInt>()) {
                TODO()
            }
        }
        EVP_MD_CTX_free(ptr)
        ptr = null
        return out
    }
}
