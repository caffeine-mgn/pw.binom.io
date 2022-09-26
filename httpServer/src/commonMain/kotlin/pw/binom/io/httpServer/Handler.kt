package pw.binom.io.httpServer

fun interface Handler {
    suspend fun request(req: HttpRequest)
}
