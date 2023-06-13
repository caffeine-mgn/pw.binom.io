package pw.binom.io.httpClient.protocol

import pw.binom.url.URL

interface ProtocolSelector {
    fun find(url: URL): ConnectFactory2?
    fun select(url: URL): ConnectFactory2 =
        find(url) ?: throw IllegalArgumentException("Can't find ConnectionFactory for schema \"${url.schema}\"")
}
