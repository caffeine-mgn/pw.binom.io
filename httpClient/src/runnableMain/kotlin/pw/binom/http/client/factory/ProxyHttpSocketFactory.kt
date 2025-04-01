package pw.binom.http.client.factory

import pw.binom.SafeException
import pw.binom.http.client.tcpConnectViaHttpProxy
import pw.binom.io.AsyncChannel
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpAuth
import pw.binom.io.http.emptyHeaders
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.socket.SocketAddress

class ProxyHttpSocketFactory(
  private val default: NetSocketFactory,
  private val proxySelector: ProxySelector,
) : NetSocketFactory {
    fun interface ProxySelector {
        fun getProxy(host: String, port: Int): ProxyConfig?
    }

    data class ProxyConfig(
        val address: SocketAddress,
        val auth: HttpAuth? = null,
        val headers: Headers = emptyHeaders(),
    )

    override suspend fun connect(host: String, port: Int): AsyncChannel {
        val proxyAddress = proxySelector.getProxy(
            host = host,
            port = port,
        ) ?: return default.connect(host = host, port = port)
        return SafeException.async {
            val channel =
                default.connect(
                    host = proxyAddress.address.host,
                    port = proxyAddress.address.port,
                ).closeOnException()
            channel.tcpConnectViaHttpProxy(
                address = DomainSocketAddress(host = host, port = port),
                auth = proxyAddress.auth,
                headers = proxyAddress.headers,
            )
        }
    }
}
