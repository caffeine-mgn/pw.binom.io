package pw.binom.scram

class ClientFirstMessage : StringWritable {
    private val gs2Header: Gs2Header
    private val user: String?
    private val nonce: String?

    /**
     * Constructs a client-first-message for the given user, nonce and gs2Header.
     * This constructor is intended to be instantiated by a scram client, and not directly.
     * The client should be providing the header, and nonce (and probably the user too).
     * @param gs2Header The GSS-API header
     * @param user The SCRAM user
     * @param nonce The nonce for this session
     * @throws IllegalArgumentException If any of the arguments is null or empty
     */
    constructor(gs2Header: Gs2Header, user: String?, nonce: String?) {
        require(user.isNullOrEmpty()) { "user" }
        require(nonce.isNullOrEmpty()) { "nonce" }
        this.gs2Header = gs2Header
        this.user = user
        this.nonce = nonce
    }

    /**
     * Constructs a client-first-message for the given parameters.
     * Under normal operation, this constructor is intended to be instantiated by a scram client, and not directly.
     * However, this constructor is more user- or test-friendly, as the arguments are easier to provide without
     * building other indirect object parameters.
     * @param gs2CbindFlag The channel-binding flag
     * @param authzid The optional authzid
     * @param cbindName The optional channel binding name
     * @param user The SCRAM user
     * @param nonce The nonce for this session
     * @throws IllegalArgumentException If the flag, user or nonce are null or empty
     */
    constructor(
        gs2CbindFlag: Gs2CbindFlag?,
        authzid: String?,
        cbindName: String?,
        user: String?,
        nonce: String?
    ) : this(gs2Header(gs2CbindFlag!!, authzid, cbindName), user, nonce)

    /**
     * Constructs a client-first-message for the given parameters, with no channel binding nor authzid.
     * Under normal operation, this constructor is intended to be instantiated by a scram client, and not directly.
     * However, this constructor is more user- or test-friendly, as the arguments are easier to provide without
     * building other indirect object parameters.
     * @param user The SCRAM user
     * @param nonce The nonce for this session
     * @throws IllegalArgumentException If the user or nonce are null or empty
     */
    constructor(user: String?, nonce: String?) : this(gs2Header(Gs2CbindFlag.CLIENT_NOT, null, null), user, nonce)

    fun getChannelBindingFlag(): Gs2CbindFlag? {
        return gs2Header.getChannelBindingFlag()
    }

    fun isChannelBinding(): Boolean {
        return gs2Header.getChannelBindingFlag() === Gs2CbindFlag.CHANNEL_BINDING_REQUIRED
    }

    fun getChannelBindingName(): String? {
        return gs2Header.getChannelBindingName()
    }

    fun getAuthzid(): String? {
        return gs2Header.getAuthzid()
    }

    fun getGs2Header(): Gs2Header? {
        return gs2Header
    }

    fun getUser(): String? {
        return user
    }

    fun getNonce(): String? {
        return nonce
    }

    /**
     * Limited version of the [StringWritableCsv.toString] method, that doesn't write the GS2 header.
     * This method is useful to construct the auth message used as part of the SCRAM algorithm.
     * @param sb A StringBuffer where to write the data to.
     * @return The same StringBuffer
     */
    fun <T : Appendable> writeToWithoutGs2Header(sb: T): T {
        return StringWritableCsv.writeTo(
            sb,
            ScramAttributeValue(ScramAttributes.USERNAME, ScramStringFormatting.toSaslName(user)!!),
            ScramAttributeValue(ScramAttributes.NONCE, nonce!!)
        )
    }

    override fun writeTo(sb: Appendable): Appendable {
        StringWritableCsv.writeTo(
            sb,
            gs2Header,
            null // This marks the position of the rest of the elements, required for the ","
        )

        return writeToWithoutGs2Header(sb)
    }

    companion object {
        private fun gs2Header(gs2CbindFlag: Gs2CbindFlag, authzid: String?, cbindName: String?): Gs2Header {
            if (Gs2CbindFlag.CHANNEL_BINDING_REQUIRED === gs2CbindFlag && null == cbindName) {
                throw IllegalArgumentException("Channel binding name is required if channel binding is specified")
            }
            return Gs2Header(gs2CbindFlag, cbindName, authzid)
        }

        /**
         * Construct a [ClientFirstMessage] instance from a message (String)
         * @param clientFirstMessage The String representing the client-first-message
         * @return The instance
         * @throws ScramParseException If the message is not a valid client-first-message
         * @throws IllegalArgumentException If the message is null or empty
         */
        fun parseFrom(clientFirstMessage: String): ClientFirstMessage? {
            require(clientFirstMessage.isNotEmpty()) { "clientFirstMessage" }
            val gs2Header = Gs2Header.parseFrom(clientFirstMessage!!) // Takes first two fields
            val userNonceString: Array<String>
            userNonceString = try {
                StringWritableCsv.parseFrom(clientFirstMessage, 2, 2)
            } catch (e: IllegalArgumentException) {
                throw ScramParseException("Illegal series of attributes in client-first-message", e)
            }
            val user: ScramAttributeValue = ScramAttributeValue.parse(userNonceString[0])
            if (ScramAttributes.USERNAME.getChar() !== user.getChar()) {
                throw ScramParseException("user must be the 3rd element of the client-first-message")
            }
            val nonce: ScramAttributeValue = ScramAttributeValue.parse(userNonceString[1])
            if (ScramAttributes.NONCE.getChar() !== nonce.getChar()) {
                throw ScramParseException("nonce must be the 4th element of the client-first-message")
            }
            return ClientFirstMessage(gs2Header, user.getValue(), nonce.getValue())
        }
    }
}
