package pw.binom.io.httpClient.protocol.v11

fun interface RequestFinishedListener {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun requestFinished(responseKeepAlive: Boolean, success: Boolean)
}
