package pw.binom.http.client

import pw.binom.date.DateTime
import pw.binom.io.AsyncCloseable
import pw.binom.io.http.Headers

/**
 * * Держатель HTTP подключения
 */
interface HttpConnection : AsyncCloseable {
    enum class State {
        READY,
        BUSY,
        CLOSED,
    }

    val state: State
    val lastActive: DateTime
    suspend fun connect(
        method: String,
        request: String,
        headers: Headers,
    ): HttpClientExchange
}
