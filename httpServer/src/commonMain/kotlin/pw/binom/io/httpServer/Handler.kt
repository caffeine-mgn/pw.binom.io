package pw.binom.io.httpServer

@Deprecated(message = "Use HttpServer2")
fun interface Handler {
    suspend fun request(req: HttpRequest)
}
