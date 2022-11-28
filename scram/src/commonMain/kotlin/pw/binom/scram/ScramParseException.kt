package pw.binom.scram

/**
 * This class represents an error when parsing SCRAM messages
 */
open class ScramParseException : ScramException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
