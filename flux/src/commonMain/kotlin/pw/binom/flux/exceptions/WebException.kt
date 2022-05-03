package pw.binom.flux.exceptions

import pw.binom.io.httpServer.HttpResponse

abstract class WebException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    /**
     * Calls when request processing method throw exception. Can change response of server
     */
    open fun processing(response: HttpResponse) {
        response.status = 500
    }
}
