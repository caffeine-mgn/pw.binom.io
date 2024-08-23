package pw.binom.security

import pw.binom.io.ByteBuffer
import pw.binom.ssl.Key

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface Signature {
    companion object {
        fun getInstance(algorithm: String): Signature
    }

    fun init(key: Key.Private)
    fun init(key: Key.Public)
    fun update(data: ByteArray)
    fun update(data: ByteBuffer)
    fun sign(): ByteArray
    fun verify(signature: ByteArray): Boolean
}
