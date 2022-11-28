package pw.binom.scram

/**
 * GSS Header. Format:
 *
 * {@code
 * gs2-header      = gs2-cbind-flag "," [ authzid ] ","
 * gs2-cbind-flag  = ("p=" cb-name) / "n" / "y"
 * authzid         = "a=" saslname
 * }
 *
 * Current implementation does not support channel binding.
 * If p is used as the cbind flag, the cb-name value is not validated.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
class Gs2Header : AbstractStringWritable {
    private val cbind: Gs2AttributeValue?
    private val authzid: Gs2AttributeValue?

    /**
     * Construct and validates a Gs2Header.
     * Only provide the channel binding name if the channel binding flag is set to required.
     * @param cbindFlag The channel binding flag
     * @param cbName The channel-binding name. Should be not null iif channel binding is required
     * @param authzid The optional SASL authorization identity
     * @throws IllegalArgumentException If the channel binding flag and argument are invalid
     */
    constructor(cbindFlag: Gs2CbindFlag, cbName: String?, authzid: String?) {
        if ((cbindFlag === Gs2CbindFlag.CHANNEL_BINDING_REQUIRED) xor (cbName != null)) {
            throw IllegalArgumentException("Specify channel binding flag and value together, or none")
        }
        // TODO: cbName is not being properly validated
        this.cbind = Gs2AttributeValue(Gs2Attributes.byGS2CbindFlag(cbindFlag), cbName!!)
        this.authzid = if (authzid == null) null else Gs2AttributeValue(
            Gs2Attributes.AUTHZID,
            ScramStringFormatting.toSaslName(authzid)
        )
    }

    /**
     * Construct and validates a Gs2Header with no authzid.
     * Only provide the channel binding name if the channel binding flag is set to required.
     * @param cbindFlag The channel binding flag
     * @param cbName The channel-binding name. Should be not null iif channel binding is required
     * @throws IllegalArgumentException If the channel binding flag and argument are invalid
     */
    constructor(cbindFlag: Gs2CbindFlag, cbName: String?) : this(cbindFlag = cbindFlag, cbName = cbName, authzid = null)

    /**
     * Construct and validates a Gs2Header with no authzid nor channel binding.
     * @param cbindFlag The channel binding flag
     * @throws IllegalArgumentException If the channel binding is supported (no cbname can be provided here)
     */
    constructor(cbindFlag: Gs2CbindFlag) : this(cbindFlag = cbindFlag, cbName = null, authzid = null)

    override fun writeTo(sb: Appendable): Appendable = StringWritableCsv.writeTo(sb, cbind, authzid)

    fun getChannelBindingFlag(): Gs2CbindFlag? {
        return Gs2CbindFlag.byChar(cbind!!.getChar())
    }

    fun getChannelBindingName(): String? {
        return cbind!!.getValue()
    }

    fun getAuthzid(): String? {
        return authzid?.getValue()
    }

    companion object {
        /**
         * Read a Gs2Header from a String. String may contain trailing fields that will be ignored.
         * @param message The String containing the Gs2Header
         * @return The parsed Gs2Header object
         * @throws IllegalArgumentException If the format/values of the String do not conform to a Gs2Header
         */
        fun parseFrom(message: String): Gs2Header {
            val gs2HeaderSplit: Array<String> = StringWritableCsv.parseFrom(message, 2)
            if (gs2HeaderSplit.isEmpty()) {
                throw IllegalArgumentException("Invalid number of fields for the GS2 Header")
            }
            val gs2cbind: Gs2AttributeValue = Gs2AttributeValue.parse(gs2HeaderSplit[0])
            return Gs2Header(
                Gs2CbindFlag.byChar(gs2cbind.getChar())!!,
                gs2cbind.getValue(),
                if (gs2HeaderSplit[1].isEmpty()) null else Gs2AttributeValue.parse(
                    gs2HeaderSplit[1]
                ).getValue()
            )
        }
    }
}
