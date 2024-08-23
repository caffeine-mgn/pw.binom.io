package pw.binom.ssl

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface Cipher {
    companion object {
        fun getInstance(transformation: String): Cipher
    }

    enum class Mode {
        ENCODE,
        DECODE,
    }

    fun init(mode: Mode, key: Key)
    fun doFinal(data: ByteArray): ByteArray
}
