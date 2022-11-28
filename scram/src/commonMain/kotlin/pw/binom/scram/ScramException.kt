package pw.binom.scram

/**
 * This class represents an error when using SCRAM, which is a SASL method.
 *
 * [SaslException]
 */
open class ScramException : SaslException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
