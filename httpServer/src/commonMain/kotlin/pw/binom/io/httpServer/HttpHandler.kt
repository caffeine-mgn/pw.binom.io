package pw.binom.io.httpServer

fun interface HttpHandler {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun handle(exchange: HttpServerExchange)
}
