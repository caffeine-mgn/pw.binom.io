package pw.binom.db.postgresql.async

import pw.binom.db.SQLException

class PostgresqlException : SQLException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}