package pw.binom.flux

fun interface FluxHttpHandler {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun handle(exchange: FluxHttpServerExchange)
}
