package pw.binom.io.httpServer

/**
 * Http Server Request Handler
 */
interface Handler3 {

    /**
     * Calling when server have new request
     *
     * @param req Request object
     * @param resp Response object
     */
    suspend fun request(req: HttpRequest, resp: HttpResponse)
}

fun Handler3(func: suspend (req: HttpRequest, resp: HttpResponse) -> Unit) = object : Handler3 {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        func(req, resp)
    }

}