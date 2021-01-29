package pw.binom.network

import pw.binom.io.IOException

open class SocketClosedException : IOException{
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}