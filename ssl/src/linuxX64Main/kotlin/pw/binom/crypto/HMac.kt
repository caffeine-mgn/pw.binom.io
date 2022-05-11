package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.ByteBuffer
import pw.binom.io.MessageDigest

actual class HMac actual constructor(val algorithm: Algorithm, val key: ByteArray) : MessageDigest {
    private var ctx: CPointer<HMAC_CTX>? = null

    actual enum class Algorithm(val size: Int, val make: () -> CPointer<EVP_MD>) {
        SHA512(64, { EVP_sha512()!! }), SHA256(32, { EVP_sha256()!! }), SHA1(20, { EVP_sha1()!! }), MD5(
            16,
            { EVP_md5()!! }
        )
    }

    private fun checkInit() {
        if (this.ctx != null) {
            return
        }
        val ctx = HMAC_CTX_new()!!
        HMAC_Init_ex(ctx, key.refTo(0), key.size, algorithm.make(), null)
        this.ctx = ctx
    }

    override fun init() {
        if (ctx != null) {
            HMAC_CTX_free(ctx)
            ctx = null
        }
        checkInit()
    }

    override fun update(byte: Byte) {
        update(ByteArray(1) { byte })
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        checkInit()
        memScoped {
            HMAC_Update(ctx, input.refTo(offset).getPointer(this).reinterpret(), len.convert())
        }
    }

    override fun update(buffer: ByteBuffer) {
        checkInit()
        memScoped {
            buffer.ref { bufferPtr, remaining ->
                HMAC_Update(ctx, bufferPtr.getPointer(this).reinterpret(), remaining.convert())
            }
        }
    }

    override fun finish(): ByteArray {
        checkInit()
        val out = ByteArray(algorithm.size)
        out.usePinned { outPinned ->
            memScoped {
                val size = alloc<UIntVar>()
                HMAC_Final(ctx, outPinned.addressOf(0).getPointer(this).reinterpret(), size.ptr)
                if (size.value != out.size.convert<UInt>()) {
                    TODO()
                }
                HMAC_CTX_free(ctx)
            }
        }
        return out
    }
}
