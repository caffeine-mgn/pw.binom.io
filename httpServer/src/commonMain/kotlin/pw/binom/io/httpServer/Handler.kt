package pw.binom.io.httpServer

fun interface Handler {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun request(req: HttpRequest)
}
