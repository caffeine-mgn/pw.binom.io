package pw.binom.crypto

interface Cipher {
    enum class Mode {
        DECRYPT,
        ENCRYPT,
    }

    fun init(mode: Mode)

    companion object {
    }
}
