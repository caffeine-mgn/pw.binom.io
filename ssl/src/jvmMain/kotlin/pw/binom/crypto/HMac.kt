package pw.binom.crypto

import pw.binom.ByteBuffer
import pw.binom.io.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual class HMac actual constructor(algorithm: Algorithm, key: ByteArray) : MessageDigest {
    val native = Mac.getInstance(algorithm.code)

    init {
        native.init(SecretKeySpec(key, algorithm.code))
    }

    actual enum class Algorithm(val code: String) {
        SHA512("HmacSHA512"), SHA256("HmacSHA256"), SHA1("HmacSHA1"), MD5("HmacMD5")
    }

    override fun init() {
        native.reset()
    }

    override fun update(byte: Byte) {
        native.update(byte)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        native.update(input, offset, len)
    }

    override fun update(byte: ByteArray) {
        native.update(byte)
    }

    override fun update(buffer: ByteBuffer) {
        native.update(buffer.native)
    }

    override fun finish(): ByteArray =
        native.doFinal()
}