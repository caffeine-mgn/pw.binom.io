package pw.binom.http.client.factory

import pw.binom.SafeException
import pw.binom.http.client.tcpConnectViaHttpProxy
import pw.binom.io.AsyncChannel
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpAuth
import pw.binom.io.http.emptyHeaders
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.socket.SocketAddress

class HttpProxyNetSocketFactory(
  private val fallback: NetSocketFactory,
  private val proxySelector: ProxySelector,
) : NetSocketFactory {

    fun interface ProxySelector {
        fun getProxyConfig(host: String, port: Int): ProxyConfig?
    }

    data class ProxyConfig(
        val address: SocketAddress,
        val auth: HttpAuth? = null,
        val headers: Headers = emptyHeaders(),
    )

    override suspend fun connect(host: String, port: Int): AsyncChannel {
        val proxyAddress = proxySelector.getProxyConfig(host, port)
            ?: return fallback.connect(host = host, port = port)
        return SafeException.async {
            val channel =
                fallback.connect(
                    host = host, port = port
                ).closeOnException()
            channel.tcpConnectViaHttpProxy(
                address = DomainSocketAddress(host = host, port = port),
                auth = proxyAddress.auth,
                headers = proxyAddress.headers,
            )
        }
    }
}
