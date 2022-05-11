package pw.binom.ssl

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
