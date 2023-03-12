package pw.binom.io.httpServer

@Deprecated(message = "Use HttpServer2")
abstract class HandlerInterceptor(val handler: Handler) : Handler {

    protected abstract suspend fun catch(req: HttpRequest)

    protected suspend fun invokeSuper(req: HttpRequest) {
        handler.request(req)
    }

    override suspend fun request(req: HttpRequest) {
        catch(req)
    }
}
