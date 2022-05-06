package pw.binom.ssl

import kotlinx.cinterop.*
import kotlinx.wasm.jsinterop.allocateArena
import platform.openssl.*
import kotlin.native.internal.createCleaner

actual interface Cipher {
    actual companion object {
        actual fun getInstance(transformation: String): Cipher =
            when (transformation) {
                "RSA" -> RsaCipherImpl()
                else -> TODO("Unknown transformation \"$transformation\"")
            }
    }

    actual enum class Mode {
        ENCODE, DECODE,
    }

    actual fun init(mode: Mode, key: Key)
    actual fun doFinal(data: ByteArray): ByteArray
}

class RsaCipherImpl : Cipher {
    init {
        loadOpenSSL()
    }

    var rsa: CPointer<RSA>? = null

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(rsa) { rsa ->
        RSA_free(rsa)
    }

    private var mode = Cipher.Mode.ENCODE
    private var isPublicKey = false

    override fun init(mode: Cipher.Mode, key: Key) {
        this.mode = mode
        val rsa = when (key) {
            is Key.Public -> {
                isPublicKey = true
                createRsaFromPublicKey(key.data)
            }
            is Key.Private -> {
                isPublicKey = false
                createRsaFromPrivateKey(key.data)
            }
        }
        buffer = ByteArray(rsa.dataSize)
        this.rsa = rsa
    }

    private lateinit var buffer: ByteArray

    override fun doFinal(data: ByteArray): ByteArray {
        println("--->#1")
        val result = buffer.usePinned { output ->
            data.usePinned { pinned ->
                println("--->#2")
                if (isPublicKey) {
                    if (mode == Cipher.Mode.ENCODE) {
                        RSA_public_encrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            RSA_PKCS1_OAEP_PADDING,
                        )
                    } else {
                        RSA_public_decrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            RSA_PKCS1_OAEP_PADDING,
                        )
                    }
                } else {
                    if (mode == Cipher.Mode.ENCODE) {
                        RSA_private_encrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            RSA_PKCS1_OAEP_PADDING,
                        )
                    } else {
                        RSA_private_decrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            RSA_PKCS1_OAEP_PADDING,
                        )
                    }
                }
            }
        }
        if (result <= 0) {
            TODO("result: $result")
        }
        return buffer.copyOfRange(0, result)
    }
}
