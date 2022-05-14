package pw.binom.io.http

import pw.binom.network.SocketClosedException

open class HttpConnectionClosedException : SocketClosedException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
