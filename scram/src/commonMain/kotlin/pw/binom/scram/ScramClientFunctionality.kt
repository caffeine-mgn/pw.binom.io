package pw.binom.scram

/**
 * Provides building blocks for creating SCRAM authentication client
 */
interface ScramClientFunctionality {
    /**
     * Prepares the first client message
     * @param username Username of the user
     * @return First client message
     * @throws ScramException if username contains prohibited characters
     */
    @Throws(ScramException::class)
    fun prepareFirstMessage(username: String): String

    /**
     * Prepares client's final message
     * @param password User password
     * @param serverFirstMessage Server's first message
     * @return Client's final message
     * @throws ScramException if there is an error processing server's message, i.e. it violates the protocol
     */
    @Throws(ScramException::class)
    fun prepareFinalMessage(password: String?, serverFirstMessage: String): String?

    /**
     * Checks if the server's final message is valid
     * @param serverFinalMessage Server's final message
     * @return true if the server's message is valid, false otherwise
     */
    @Throws(ScramException::class)
    fun checkServerFinalMessage(serverFinalMessage: String): Boolean

    /**
     * Checks if authentication is successful.
     * You can call this method only if authentication is completed. Ensure that using [.isEnded]
     * @return true if successful, false otherwise
     */
    fun isSuccessful(): Boolean

    /**
     * Checks if authentication is completed, either successfully or not.
     * Authentication is completed if [.getState] returns ENDED.
     * @return true if authentication has ended
     */
    fun isEnded(): Boolean

    /**
     * Gets the state of the authentication procedure
     * @return Current state
     */
    fun getState(): State?

    /**
     * State of the authentication procedure
     */
    enum class State {
        /**
         * Initial state
         */
        INITIAL,

        /**
         * State after first message is prepared
         */
        FIRST_PREPARED,

        /**
         * State after final message is prepared
         */
        FINAL_PREPARED,

        /**
         * Authentication is completes, either successfully or not
         */
        ENDED
    }
}
