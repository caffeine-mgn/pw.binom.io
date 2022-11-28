package pw.binom.scram

/**
 * Possible values of a GS2 Attribute.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
enum class Gs2Attributes(val flag: Char) : CharAttribute {
    /**
     * Channel binding attribute. Client doesn't support channel binding.
     */
    CLIENT_NOT(Gs2CbindFlag.CLIENT_NOT.getChar()),

    /**
     * Channel binding attribute. Client does support channel binding but thinks the server does not.
     */
    CLIENT_YES_SERVER_NOT(Gs2CbindFlag.CLIENT_YES_SERVER_NOT.getChar()),

    /**
     * Channel binding attribute. Client requires channel binding. The selected channel binding follows "p=".
     */
    CHANNEL_BINDING_REQUIRED(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED.getChar()),

    /**
     * SCRAM attribute. This attribute specifies an authorization identity.
     */
    AUTHZID(ScramAttributes.AUTHZID.getChar());

    override fun getChar(): Char {
        return flag
    }

    companion object {
        fun byChar(c: Char): Gs2Attributes {
            when (c) {
                'n' -> return CLIENT_NOT
                'y' -> return CLIENT_YES_SERVER_NOT
                'p' -> return CHANNEL_BINDING_REQUIRED
                'a' -> return AUTHZID
            }
            throw IllegalArgumentException("Invalid GS2Attribute character '$c'")
        }

        fun byGS2CbindFlag(cbindFlag: Gs2CbindFlag): Gs2Attributes = byChar(cbindFlag.getChar())
    }
}
