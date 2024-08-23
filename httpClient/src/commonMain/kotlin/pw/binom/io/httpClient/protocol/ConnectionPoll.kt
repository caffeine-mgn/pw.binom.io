package pw.binom.io.httpClient.protocol

fun interface ConnectionPoll {
    suspend fun recycle(key: String, connect: HttpConnect)
}
