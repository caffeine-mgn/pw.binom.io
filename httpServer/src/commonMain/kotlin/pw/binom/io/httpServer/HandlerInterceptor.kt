package pw.binom.io.httpServer

abstract class HandlerInterceptor(val handler: Handler) : Handler {

    protected abstract suspend fun catch(req: HttpRequest, resp: HttpResponse)

    protected suspend fun invokeSuper(req: HttpRequest, resp: HttpResponse) {
        handler.request(req, resp)
    }

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        catch(req, resp)
    }
}