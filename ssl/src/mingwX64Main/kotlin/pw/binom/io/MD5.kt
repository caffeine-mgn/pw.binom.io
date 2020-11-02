package pw.binom.io

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.ByteBuffer

actual class MD5 : MessageDigest {
    private var ptr: CPointer<EVP_MD_CTX>? = null
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
            EVP_DigestInit_ex(ptr, EVP_md5(), null)
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
        EVP_DigestUpdate(ptr, buffer.native + buffer.position, buffer.remaining.convert())
        buffer.position = buffer.capacity
    }

    override fun finish(): ByteArray {
        checkInit()
        val out = ByteArray(16)
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