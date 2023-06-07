package pw.binom.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.openssl.*
import pw.binom.checkTrue
import pw.binom.crypto.RsaPadding
import kotlin.native.internal.Cleaner
import kotlin.native.internal.createCleaner

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
            else -> TODO()
        }
        buffer = ByteArray(rsa.dataSize)
        println("rsa.dataSize=${rsa.dataSize}")
        this.rsa = rsa
        cleaner = createCleaner(rsa) { rsa2 ->
            RSA_free(rsa2)
        }
    }

    private lateinit var buffer: ByteArray

    override fun doFinal(data: ByteArray): ByteArray {
        if (mode == Cipher.Mode.ENCODE && data.size > rsa!!.dataSize - padding.size) {
            throw IllegalArgumentException("Data should be less then ${rsa!!.dataSize - padding.size}. Actual size: ${data.size}")
        }
        val result = buffer.usePinned { output ->
            data.usePinned { pinned ->
                if (isPublicKey) {
                    if (mode == Cipher.Mode.ENCODE) {
                        RSA_public_encrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
                        ).checkTrue("RSA_public_encrypt fail")
                    } else {
                        RSA_public_decrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
                        ).checkTrue("RSA_public_decrypt fail")
                    }
                } else {
                    if (mode == Cipher.Mode.ENCODE) {
                        RSA_private_encrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
                        ).checkTrue("RSA_private_encrypt fail")
                    } else {
                        RSA_private_decrypt(
                            pinned.get().size,
                            pinned.addressOf(0).reinterpret(),
                            output.addressOf(0).reinterpret(),
                            rsa,
                            padding.id,
                        ).checkTrue("RSA_private_decrypt fail")
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
