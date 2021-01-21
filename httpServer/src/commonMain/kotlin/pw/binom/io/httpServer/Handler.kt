package pw.binom.io.httpServer

/**
 * Http Server Request Handler
 */
interface Handler {

    /**
     * Calling when server have new request
     *
     * @param req Request object
     * @param resp Response object
     */
    suspend fun request(req: HttpRequest, resp: HttpResponse)
}

fun Handler(func: suspend (req: HttpRequest, resp: HttpResponse) -> Unit) = object : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        func(req, resp)
    }

}