package pw.binom.scram

interface ScramSaslClientProcessor {
    /**
     * Initiates the SCRAM sequence by preparing and sending the first client message
     * @param username username of the user
     * @param password password of the user
     * @throws ScramException if username contains forbidden characters (@see https://tools.ietf.org/html/rfc4013)
     */
    @Throws(ScramException::class)
    fun start(username: String, password: String?)

    /**
     * Called when message from server is received
     * @param message Message
     * @throws ScramException if there is a unrecoverable error during internal processing of the message
     */
    fun onMessage(message: String)

    /**
     * Aborts the procedure
     */
    fun abort()

    /**
     * Checks if authentication sequence has ended
     * @return true if authentication has ended, false otherwise
     */
    fun isEnded(): Boolean

    /**
     * Checks if authentication sequence has ended successfully (i.e. user is authenticated)
     * @return true if authentication sequence has ended successfully, false otherwise
     */
    fun isSuccess(): Boolean

    /**
     * Checks if the sequence has been aborted
     * @return true if aborted, false otherwise
     */
    fun isAborted(): Boolean

    /**
     * Listener for success or failure of the SCRAM SASL authentication
     */
    interface Listener {
        /**
         * Called if the authentication completed successfully
         */
        fun onSuccess()

        /**
         * Called if the authentication failed
         */
        fun onFailure()
    }

    /**
     * Provides functionality for sending message to the server
     */
    interface Sender {
        /**
         * Sends message to the server
         * @param msg Message
         */
        fun sendMessage(msg: String?)
    }
}
