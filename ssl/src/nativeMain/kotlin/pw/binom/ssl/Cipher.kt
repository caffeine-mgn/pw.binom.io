package pw.binom.ssl

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
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
        "ECDSA" -> ECCipherImpl(transformation.replace('/', '-'))
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
