package pw.binom.ssl

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.*
import platform.openssl.*
import platform.posix.memcpy
import pw.binom.BigNumContext
import pw.binom.checkTrue
import pw.binom.throwError
import pw.binom.toBigNum

abstract class OpenSSLCipher : Cipher {
    init {
        loadOpenSSL()
    }

    protected abstract val algoritm: String

    protected var ctx: CPointer<EVP_CIPHER_CTX>? = null
    protected var cipher: CPointer<EVP_CIPHER>? = null

    protected fun free() {
        if (cipher != null) {
            EVP_CIPHER_free(cipher)
            cipher = null
        }
        if (ctx != null) {
            EVP_CIPHER_CTX_free(ctx)
            ctx = null
        }
    }

    override fun doFinal(data: ByteArray): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        EVP_CipherFinal_ex(ctx, null, outLen.ptr).checkTrue("EVP_CipherFinal_ex fails")
        val output = ByteArray(outLen.value)
        output.usePinned { outputPinned ->
            EVP_CipherFinal_ex(
                ctx, outputPinned.addressOf(0).reinterpret(), outLen.ptr
            ).checkTrue("EVP_CipherFinal_ex fails")
        }
        free()
        output
    }

    protected fun init(mode: Cipher.Mode, key: COpaquePointer, params: Map<String, Any>) {
        free()
        memScoped {
            BigNumContext().use { bnCtx ->
                var c = 0
                val paramsPtr = allocArray<OSSL_PARAM>(params.size + 1)
                params.forEach {
                    val param = when (val value = it.value) {
                        is String -> {
                            OSSL_PARAM_construct_utf8_string(
                                it.key, value.cstr, 0
                            )
                        }
                        is BigInteger -> {
                            val bb = value.toBigNum(bnCtx)
                            OSSL_PARAM_construct_BN(it.key, bb.ptr.reinterpret(), bb.sizeInBytes.convert())
                        }
                        is Int -> {
                            val intValue = alloc<IntVar>()
                            intValue.value = value
                            OSSL_PARAM_construct_int32(it.key, intValue.ptr)
                        }
                        is Long -> {
                            val longValue = alloc<LongVar>()
                            longValue.value = value
                            OSSL_PARAM_construct_int64(it.key, longValue.ptr)
                        }
                        else -> throw IllegalArgumentException("Can't pass value of ${it.key}: type ${value::class.simpleName} not supported")
                    }
                    memcpy(paramsPtr[c++].ptr, param.ptr, sizeOf<OSSL_PARAM>().convert())
                }
                val end = OSSL_PARAM_construct_end()
                memcpy(paramsPtr[c++].ptr, end.ptr, sizeOf<OSSL_PARAM>().convert())
                val ctx = EVP_CIPHER_CTX_new() ?: throwError("EVP_CIPHER_CTX_new fails")
                val cipher = EVP_CIPHER_fetch(null, algoritm, null) ?: throwError("EVP_CIPHER_fetch fails")
                EVP_CipherInit_ex2(
                    ctx = ctx,
                    cipher = cipher,
                    key = key.reinterpret(),
                    iv = null,
                    enc = if (mode == Cipher.Mode.ENCODE) 1 else 0,
                    params = if (params.isEmpty()) null else paramsPtr,
                ).checkTrue("EVP_CipherInit_ex2 fails")
                this@OpenSSLCipher.ctx = ctx
                this@OpenSSLCipher.cipher = cipher
            }
        }
    }
}
