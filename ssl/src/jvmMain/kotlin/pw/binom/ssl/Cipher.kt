package pw.binom.ssl

import org.bouncycastle.crypto.encodings.PKCS1Encoding
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher as JvmCipher

actual interface Cipher {
    actual companion object {
        actual fun getInstance(transformation: String): Cipher =
            CipherJvm(JvmCipher.getInstance(transformation))
    }

    actual enum class Mode(val code: Int) {
        ENCODE(JvmCipher.ENCRYPT_MODE), DECODE(JvmCipher.DECRYPT_MODE),
    }

    actual fun init(mode: Mode, key: Key)
    actual fun doFinal(data: ByteArray): ByteArray
}

class CipherJvm(val native: JvmCipher) : Cipher {
    override fun init(mode: Cipher.Mode, key: Key) {
        val jvmKey = when (key.algorithm) {
            KeyAlgorithm.RSA -> {
                val c = KeyFactory.getInstance("RSA")
                when (key) {
                    is Key.Public -> c.generatePublic(X509EncodedKeySpec(key.data))
                    is Key.Private -> c.generatePrivate(PKCS8EncodedKeySpec(key.data))
                }
            }
        }

        native.init(mode.code, jvmKey)
    }

    override fun doFinal(data: ByteArray): ByteArray = native.doFinal(data)
}
