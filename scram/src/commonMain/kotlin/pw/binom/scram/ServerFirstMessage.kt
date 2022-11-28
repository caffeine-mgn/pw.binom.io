package pw.binom.scram

/**
 * Constructs and parses server-first-messages. Formal syntax is:
 *
 * {@code
 * server-first-message = [reserved-mext ","] nonce "," salt ","
 *                        iteration-count ["," extensions]
 * }
 *
 * Note that extensions are not supported.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
class ServerFirstMessage : StringWritable {
    companion object {
        const val ITERATION_MIN_VALUE = 4096

        /**
         * Parses a server-first-message from a String.
         * @param serverFirstMessage The string representing the server-first-message
         * @param clientNonce The serverNonce that is present in the client-first-message
         * @return The parsed instance
         * @throws ScramParseException If the argument is not a valid server-first-message
         * @throws IllegalArgumentException If either argument is empty or serverFirstMessage is not a valid message
         */
        fun parseFrom(serverFirstMessage: String, clientNonce: String): ServerFirstMessage {
            require(serverFirstMessage.isNotEmpty()) { "serverFirstMessage" }
            require(clientNonce.isNotEmpty()) { "clientNonce" }
            val attributeValues = StringWritableCsv.parseFrom(serverFirstMessage, 3, 0)
            if (attributeValues.size != 3) {
                throw ScramParseException("Invalid server-first-message")
            }
            val nonce = ScramAttributeValue.parse(attributeValues[0])
            if (ScramAttributes.NONCE.getChar() != nonce.getChar()) {
                throw ScramParseException("serverNonce must be the 1st element of the server-first-message")
            }
            if (!nonce.getValue()!!.startsWith(clientNonce)) {
                throw ScramParseException("parsed serverNonce does not start with client serverNonce")
            }
            val salt = ScramAttributeValue.parse(attributeValues[1])
            if (ScramAttributes.SALT.getChar() != salt.getChar()) {
                throw ScramParseException("salt must be the 2nd element of the server-first-message")
            }
            val iteration = ScramAttributeValue.parse(attributeValues[2])
            if (ScramAttributes.ITERATION.getChar() != iteration.getChar()) {
                throw ScramParseException("iteration must be the 3rd element of the server-first-message")
            }
            val iterationInt = iteration.getValue()!!.toIntOrNull() ?: throw ScramParseException("invalid iteration")
            return ServerFirstMessage(
                clientNonce,
                nonce.getValue()!!.substring(clientNonce.length),
                salt.getValue()!!,
                iterationInt
            )
        }
    }

    private val clientNonce: String
    private val serverNonce: String
    private val salt: String
    private val iteration: Int

    /**
     * Constructs a server-first-message from a client-first-message and the additional required data.
     * @param clientNonce String representing the client-first-message
     * @param serverNonce Server serverNonce
     * @param salt The salt
     * @param iteration The iteration count (must be &lt;= 4096)
     * @throws IllegalArgumentException If clientFirstMessage, serverNonce or salt are null or empty,
     * or iteration &lt; 4096
     */
    constructor(
        clientNonce: String,
        serverNonce: String,
        salt: String,
        iteration: Int
    ) {
        require(clientNonce.isNotEmpty()) { "clientNonce" }
        require(serverNonce.isNotEmpty()) { "serverNonce" }
        require(salt.isNotEmpty()) { "salt" }
        this.clientNonce = clientNonce
        this.serverNonce = serverNonce
        this.salt = salt
        require(iteration >= ITERATION_MIN_VALUE) { "iteration must be >= $ITERATION_MIN_VALUE" }
        this.iteration = iteration
    }

    fun getClientNonce(): String? {
        return clientNonce
    }

    fun getServerNonce(): String? {
        return serverNonce
    }

    fun getNonce() = clientNonce + serverNonce

    fun getSalt() = salt

    fun getIteration(): Int {
        return iteration
    }

    override fun writeTo(sb: Appendable): Appendable {
        return StringWritableCsv.writeTo(
            sb,
            ScramAttributeValue(ScramAttributes.NONCE, getNonce()),
            ScramAttributeValue(ScramAttributes.SALT, salt),
            ScramAttributeValue(ScramAttributes.ITERATION, iteration.toString() + "")
        )
    }

    override fun toString(): String {
        return writeTo(StringBuilder()).toString()
    }
}
