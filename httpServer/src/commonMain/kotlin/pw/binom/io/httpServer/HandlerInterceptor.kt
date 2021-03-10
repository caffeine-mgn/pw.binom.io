package pw.binom.io.httpServer

abstract class HandlerInterceptor(val handler: Handler3Deprecated) : Handler3Deprecated {

    protected abstract suspend fun catch(req: HttpRequestDeprecated, resp: HttpResponseDeprecated)

    protected suspend fun invokeSuper(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
        handler.request(req, resp)
    }

    override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
        catch(req, resp)
    }
}