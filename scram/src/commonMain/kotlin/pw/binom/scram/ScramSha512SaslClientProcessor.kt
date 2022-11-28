package pw.binom.scram

class ScramSha512SaslClientProcessor : AbstractScramSaslClientProcessor {
    /**
     * Creates new ScramSha512SaslClientProcessor
     * @param listener Listener of the client processor (this object)
     * @param sender Sender used to send messages to the server
     */
    constructor(listener: ScramSaslClientProcessor.Listener, sender: ScramSaslClientProcessor.Sender) : super(
        listener,
        sender,
        "SHA-512",
        "HmacSHA512"
    )
}
