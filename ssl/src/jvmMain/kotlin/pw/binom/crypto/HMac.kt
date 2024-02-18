package pw.binom.crypto

import pw.binom.io.ByteBuffer
import pw.binom.security.MessageDigest
import pw.binom.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual class HMac actual constructor(algorithm: AlgorithmType, key: ByteArray) : MessageDigest {
  val native = Mac.getInstance(algorithm.code)

  init {
    native.init(SecretKeySpec(key, algorithm.code))
  }

  actual enum class AlgorithmType(val code: String) {
    SHA512("HmacSHA512"), SHA256("HmacSHA256"),
    SHA384("HmacSHA384"), SHA1("HmacSHA1"),
    MD5("HmacMD5");

    actual companion object {
      private val content = HashMap<String, AlgorithmType>()

      init {
        values().forEach {
          content[it.name.lowercase()] = it
          content[it.code.lowercase()] = it
        }
      }

      actual fun find(name: String): AlgorithmType? = content[name.lowercase()]

      /**
       * @throws NoSuchAlgorithmException throws when algorithm [name] not found
       */
      actual fun get(name: String): AlgorithmType = find(name = name) ?: throw NoSuchAlgorithmException(name)
    }
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
