package pw.binom.io.httpClient.protocol.v11

fun interface RequestFinishedListener {
    suspend fun requestFinished(responseKeepAlive: Boolean, success: Boolean)
}
