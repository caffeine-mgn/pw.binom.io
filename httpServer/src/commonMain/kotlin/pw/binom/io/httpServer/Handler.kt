package pw.binom.io.httpServer

@Deprecated(message = "Use HttpServer2")
fun interface Handler {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun request(req: HttpRequest)
}
