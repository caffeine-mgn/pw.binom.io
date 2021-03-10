package pw.binom.io.httpServer

/**
 * Http Server Request Handler
 */
interface Handler3Deprecated {

    /**
     * Calling when server have new request
     *
     * @param req Request object
     * @param resp Response object
     */
    suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated)
}

fun Handler3Deprecated(func: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated) -> Unit) = object : Handler3Deprecated {
    override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
        func(req, resp)
    }

}