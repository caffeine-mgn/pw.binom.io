package pw.binom.ssl

import pw.binom.crypto.RsaPadding
import javax.crypto.Cipher as JvmCipher

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
                "RSA" -> RSACipherJvm(args)
                else -> TODO("Unknown transformation \"$transformation\"")
            }
        }
    }

    actual enum class Mode(val code: Int) {
        ENCODE(JvmCipher.ENCRYPT_MODE), DECODE(JvmCipher.DECRYPT_MODE),
    }

    actual fun init(mode: Mode, key: Key)
    actual fun doFinal(data: ByteArray): ByteArray
}

class RSACipherJvm(args: List<String>) : Cipher {
    private val native: JvmCipher

    init {
        val padding = if (args.isNotEmpty()) {
            val padding = args.last()
            RsaPadding.valueOf(padding)
        } else {
            RsaPadding.PKCS1Padding
        }
        native = JvmCipher.getInstance("RSA/ECB/${padding.jvmName}")
    }

    override fun init(mode: Cipher.Mode, key: Key) {
        val jvmKey = when (key) {
            is Key.Public -> loadPublicKey(key.algorithm, key.data)
            is Key.Private -> loadPrivateKey(key.algorithm, key.data)
        }
        native.init(mode.code, jvmKey)
    }

    override fun doFinal(data: ByteArray): ByteArray = native.doFinal(data)
}
