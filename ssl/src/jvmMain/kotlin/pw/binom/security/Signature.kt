package pw.binom.security

import pw.binom.io.ByteBuffer
import pw.binom.ssl.Key
import pw.binom.ssl.loadPrivateKey
import pw.binom.ssl.loadPublicKey
import java.security.SecureRandom
import java.security.Signature as JvmSignature

actual interface Signature {
    actual companion object {
        actual fun getInstance(algorithm: String): Signature = SignatureImpl(JvmSignature.getInstance(algorithm))
    }

    actual fun init(key: Key.Private)
    actual fun update(data: ByteArray)
    actual fun update(data: ByteBuffer)
    actual fun sign(): ByteArray
    actual fun init(key: Key.Public)
    actual fun verify(signature: ByteArray): Boolean
}

class SignatureImpl(val native: JvmSignature) : Signature {
    override fun init(key: Key.Private) {
        native.initSign(loadPrivateKey(key.algorithm, key.data), SecureRandom())
    }

    override fun init(key: Key.Public) {
        native.initVerify(loadPublicKey(key.algorithm, key.data))
    }

    override fun update(data: ByteArray) {
        native.update(data)
    }

    override fun update(data: ByteBuffer) {
        native.update(data.native)
    }

    override fun sign(): ByteArray = native.sign()
    override fun verify(signature: ByteArray): Boolean = native.verify(signature)
}
