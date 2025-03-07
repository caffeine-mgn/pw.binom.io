package pw.binom.http.client

import pw.binom.url.URL

fun interface SchemaSelector {
    companion object {
        val EMPTY = SchemaSelector { url ->
            throw IllegalStateException("Can't connect to \"$url\": schema \"${url.schema} not supported")
        }
    }

    suspend fun connect(url: URL): HttpConnection
}
