package pw.binom.db.tarantool

open class TarantoolException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
}
