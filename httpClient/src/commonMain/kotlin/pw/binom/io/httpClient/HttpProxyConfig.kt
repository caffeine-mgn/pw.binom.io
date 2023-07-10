package pw.binom.io.httpClient

import pw.binom.io.http.HttpAuth
import pw.binom.io.socket.NetworkAddress

data class HttpProxyConfig(
    val address: NetworkAddress,
    val auth: HttpAuth? = null,
)
