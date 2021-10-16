package pw.binom.db.serialization

import pw.binom.db.SQLException

class RollbackException : SQLException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}