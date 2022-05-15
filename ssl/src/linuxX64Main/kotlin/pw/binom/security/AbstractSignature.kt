package pw.binom.security

import kotlinx.cinterop.*
import platform.openssl.*
import platform.posix.size_tVar
import pw.binom.getSslError
import pw.binom.io.ByteBuffer
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

abstract class AbstractSignature(val messageDigest: CPointer<EVP_MD>) : Signature {
    protected var ctx: CPointer<EVP_MD_CTX>? = null
    protected var signMode = false
    protected abstract fun createPkey(key: Key.Private): CPointer<EVP_PKEY>
    protected abstract fun createPkey(key: Key.Public): CPointer<EVP_PKEY>
    protected fun checkCtx() = ctx ?: throw IllegalStateException("Signature not initialized")
    protected abstract fun isAlgorithmSupport(algorithm: KeyAlgorithm): Boolean
    protected fun algorithmCheck(key: Key) {
        if (!isAlgorithmSupport(key.algorithm)) {
            throw IllegalArgumentException("Can't use key ${key.algorithm} for ECDSA")
        }
    }

    override fun init(key: Key.Private) {
        algorithmCheck(key)
        val pkey = createPkey(key)
        ctx = EVP_MD_CTX_new()
        if (EVP_DigestSignInit(ctx, null, messageDigest, null, pkey) != 1) {
            EVP_MD_CTX_free(ctx)
            throw SignatureException("EVP_DigestSignInit failed")
        }
        signMode = true
    }

    override fun init(key: Key.Public) {
        algorithmCheck(key)
        val pkey = createPkey(key)
        ctx = EVP_MD_CTX_new()
        if (EVP_DigestVerifyInit(ctx, null, messageDigest, null, pkey) != 1) {
            EVP_MD_CTX_free(ctx)
            throw SignatureException("EVP_DigestVerifyInit failed")
        }
        signMode = false
    }

    override fun update(data: ByteArray) {
        val ctx = checkCtx()
        data.usePinned { mem ->
            if (EVP_DigestUpdate(ctx, mem.addressOf(0), mem.get().size.convert()) != 1) {
                throw SignatureException("EVP_DigestUpdate failed")
            }
        }
    }

    override fun update(data: ByteBuffer) {
        val ctx = checkCtx()
        data.ref { cPointer, i ->
            if (EVP_DigestUpdate(ctx, cPointer, i.convert()) != 1) {
                throw SignatureException("EVP_DigestUpdate failed")
            }
        }
        data.position = data.limit
    }

    protected fun reset() {
        val ctx = checkCtx()
        EVP_MD_CTX_free(ctx)
        this.ctx = null
    }

    override fun sign(): ByteArray {
        val ctx = checkCtx()
        if (!signMode) {
            throw IllegalStateException("Signature initialized for verify")
        }
        memScoped {
            val size = alloc<size_tVar>()
            if (EVP_DigestSignFinal(ctx, null, size.ptr) != 1) {
                throw SignatureException("EVP_DigestSignFinal failed")
            }
            val outputData = internal_OPENSSL_malloc(size.value.toInt()) ?: TODO()
            if (EVP_DigestSignFinal(ctx, outputData, size.ptr) != 1) {
                throw SignatureException("EVP_DigestSignFinal failed")
            }
            val buf = outputData.readBytes(size.value.convert())
            internal_OPENSSL_free(outputData)

//            size.value = 0.convert()
//            val output = ByteArray(size.value.toInt())
//            output.usePinned { mem ->
//                if (EVP_DigestSignFinal(ctx, mem.addressOf(0).reinterpret(), size.ptr) != 1) {
//                    throw SignatureException("EVP_DigestSignFinal failed")
//                }
//            }
            reset()
            return buf
        }
    }

    override fun verify(signature: ByteArray): Boolean {
        val ctx = checkCtx()
        if (signMode) {
            throw IllegalStateException("Signature initialized for sign")
        }
        val result = signature.usePinned { mem ->
            EVP_DigestVerifyFinal(ctx, mem.addressOf(0).reinterpret(), mem.get().size.convert())
        }
        val res = when (result) {
            1 -> true
            0 -> false
            else -> throw SignatureException("Can't verify: ${getSslError()}")
        }
        reset()
        return res
    }
}
