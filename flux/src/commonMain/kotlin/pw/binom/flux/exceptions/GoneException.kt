package pw.binom.flux.exceptions

import pw.binom.io.httpServer.HttpResponse

class GoneException : WebException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    override suspend fun processing(response: HttpResponse) {
        response.status = 410
    }
}
