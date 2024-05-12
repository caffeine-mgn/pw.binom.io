package pw.binom.io.httpClient

import pw.binom.io.http.HttpAuth
import pw.binom.io.socket.SocketAddress

data class HttpProxyConfig(
    val address: SocketAddress,
    val auth: HttpAuth? = null,
)
