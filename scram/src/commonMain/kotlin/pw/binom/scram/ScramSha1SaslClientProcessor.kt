package pw.binom.scram

class ScramSha1SaslClientProcessor : AbstractScramSaslClientProcessor {
    /**
     * Creates new ScramSha1SaslClientProcessor
     * @param listener Listener of the client processor (this object)
     * @param sender Sender used to send messages to the server
     */
    constructor(listener: ScramSaslClientProcessor.Listener, sender: ScramSaslClientProcessor.Sender) : super(
        listener,
        sender,
        "SHA-1",
        "HmacSHA1"
    )
}
