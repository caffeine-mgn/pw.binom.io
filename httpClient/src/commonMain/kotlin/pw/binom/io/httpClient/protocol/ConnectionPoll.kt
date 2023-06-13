package pw.binom.io.httpClient.protocol

fun interface ConnectionPoll {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun recycle(key: String, connect: HttpConnect)
}
