package pw.binom.scram

enum class Gs2CbindFlag(val flag: Char) {
    /**
     * Client doesn't support channel binding.
     */
    CLIENT_NOT('n'),

    /**
     * Client does support channel binding but thinks the server does not.
     */
    CLIENT_YES_SERVER_NOT('y'),

    /**
     * Client requires channel binding. The selected channel binding follows "p=".
     */
    CHANNEL_BINDING_REQUIRED('p')
    ;

    fun getChar() = flag

    companion object {
        fun byChar(c: Char): Gs2CbindFlag? {
            when (c) {
                'n' -> return CLIENT_NOT
                'y' -> return CLIENT_YES_SERVER_NOT
                'p' -> return CHANNEL_BINDING_REQUIRED
            }
            throw IllegalArgumentException("Invalid Gs2CbindFlag character '$c'")
        }
    }
}
