package pw.binom.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.openssl.*
import pw.binom.crypto.RsaPadding
import kotlin.native.internal.Cleaner
import kotlin.native.internal.createCleaner

actual interface Cipher {
    actual companion object {
        actual fun getInstance(transformation: String): Cipher {
            val fullArgs = transformation.split("/")
            val args = if (fullArgs.size == 1) {
                emptyList()
            } else {
                fullArgs.subList(1, fullArgs.lastIndex)
            }
            return when (fullArgs[0]) {
                "RSA" -> RsaCipherImpl(args)
                else -> TODO("Unknown transformation \"$transformation\"")
            }
        }
    }

    actual enum class Mode {
        ENCODE, DECODE,
    }

    actual fun init(mode: Mode, key: Key)
    actual fun doFinal(data: ByteArray): ByteArray
}

@OptIn(ExperimentalStdlibApi::class)
class RsaCipherImpl(args: List<String>) : Cipher {
    init {
        loadOpenSSL()
    }

    private val padding = if (args.isNotEmpty()) {
        val padding = args.last()
        RsaPadding.valueOf(padding)
    } else {
        RsaPadding.PKCS1Padding
    }

    var rsa: CPointer<RSA>? = null

    private var cleaner: Cleaner? = null

    private var mode = Cipher.Mode.ENCODE
    private var isPublicKey = false
//    private val padding: Int = RSA_PKCS1_PADDING_SIZE // RSA_PKCS1_OAEP_PADDING

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
        println("rsa.dataSize=${rsa.dataSize}")
        this.rsa = rsa
        cleaner = createCleaner(rsa) { rsa ->
            RSA_free(rsa)
        }
    }

    private lateinit var buffer: ByteArray

    override fun doFinal(data: ByteArray): ByteArray {
        if (mode == Cipher.Mode.ENCODE && data.size > rsa!!.dataSize - padding.size) {
            throw IllegalArgumentException("Data should be less then ${rsa!!.dataSize - padding.size}. Actual size: ${data.size}")
        }
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
                            padding.id,
                        )
                    } else {
                        RSA_public_decrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
                        )
                    }
                } else {
                    if (mode == Cipher.Mode.ENCODE) {
                        RSA_private_encrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
                        )
                    } else {
                        RSA_private_decrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
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
