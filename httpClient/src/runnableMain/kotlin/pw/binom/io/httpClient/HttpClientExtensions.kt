package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.io.httpClient.protocol.ProtocolSelectorBySchema
import pw.binom.io.httpClient.protocol.httpproxy.HttpProxyConnectFactory2
import pw.binom.io.httpClient.protocol.ssl.HttpSSLConnectFactory2
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.url.URL

internal actual fun internalCreateHttpClient(): HttpClient = HttpClient.create()

fun HttpClient.Companion.create(
  networkDispatcher: NetworkManager = Dispatchers.Network,
  useKeepAlive: Boolean = true,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  bufferCapacity: Int = 16,
  proxy: HttpProxyConfig? = null,
  connectFactory: ConnectionFactory = ConnectionFactory.DEFAULT,
): BaseHttpClient {
  val baseProtocolSelector = ProtocolSelectorBySchema()
  val http = Http11ConnectFactory2(networkManager = networkDispatcher, connectFactory = connectFactory)
  baseProtocolSelector.set(
    http,
    "http",
    "ws",
  )
  val protocolSelector = ProtocolSelectorBySchema()
  protocolSelector.set(
    HttpSSLConnectFactory2(networkManager = networkDispatcher, protocolSelector = baseProtocolSelector),
    "https",
    "wss",
  )
  protocolSelector.set(
    http,
    "http",
    "ws",
  )
  var pp: ProtocolSelector = protocolSelector
  if (proxy != null) {
    val proxyFactory = HttpProxyConnectFactory2(
      proxyUrl = proxy.address,
      networkManager = networkDispatcher,
      protocolSelector = protocolSelector,
      auth = proxy.auth,
    )
    pp = object : ProtocolSelector {
      override fun find(url: URL) = proxyFactory
    }
  }
  return BaseHttpClient(
    useKeepAlive = useKeepAlive,
    bufferSize = bufferSize,
    bufferCapacity = bufferCapacity,
    requestHook = proxy?.let { RequestHook.HttpProxy(it.address) } ?: RequestHook.Default,
    protocolSelector = pp,
  )
}
