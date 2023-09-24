package pw.binom.io.httpClient
/*
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncWriter
import pw.binom.io.http.BasicAuth
import pw.binom.io.http.Headers
import pw.binom.io.http.Utils
import pw.binom.io.http.forEachHeader
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.url.URL

class HttpProxyConnectionFactory(
    val proxyAddress: URL,
    val auth: BasicAuth?,
) : ConnectionFactory {
    override suspend fun connect(
        networkManager: NetworkManager,
        schema: String,
        host: String,
        port: Int,
    ): AsyncChannel {
        return networkManager.tcpConnect(
            NetworkAddress.create(
                host = proxyAddress.host,
                port = proxyAddress.port ?: proxyAddress.getPort(),
            ),
        )
    }
/*
    override suspend fun writeRequest(
        output: AsyncWriter,
        method: String,
        headers: Headers,
        url: URL,
        request: String,
    ) {
        output.append(method).append(" ").append(request).append(" ").append("HTTP/1.1").append(Utils.CRLF)
        if (auth != null) {
            output.append(Headers.PROXY_AUTHORIZATION).append(": ").append(auth.headerValue).append(Utils.CRLF)
        }
        headers.forEachHeader { key, value ->
            output.append(key).append(": ").append(value).append(Utils.CRLF)
        }
        output.append(Utils.CRLF)
    }
    */
}
*/
