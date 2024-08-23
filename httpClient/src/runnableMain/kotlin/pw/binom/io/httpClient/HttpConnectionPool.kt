package pw.binom.io.httpClient

import pw.binom.io.AsyncCloseable
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.url.URL

interface HttpConnectionPool : AsyncCloseable {
    fun interface Factory {
        suspend fun connect(factory: HttpConnectionPool, url: URL): HttpConnect
    }

    suspend fun borrow(url: URL, factory: Factory): HttpConnect

    suspend fun recycle(url: URL, channel: HttpConnect)
}
