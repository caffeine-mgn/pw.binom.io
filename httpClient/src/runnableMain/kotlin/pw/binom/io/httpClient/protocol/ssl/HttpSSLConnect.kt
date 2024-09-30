package pw.binom.io.httpClient.protocol.ssl

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpRequestBody
import pw.binom.io.httpClient.getPort
import pw.binom.io.httpClient.protocol.ConnectionPoll
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.ssl.SSLContext
import pw.binom.url.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class HttpSSLConnect(
  var channel: AsyncChannel?,
  val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
  val protocolSelector: ProtocolSelector,
  private val networkManager: NetworkManager,
  val sslContext: SSLContext,
) : HttpConnect {
  private var created = TimeSource.Monotonic.markNow()
  private var closed = false
  override val isAlive: Boolean
    get() = !closed
  override val age: Duration
    get() = created.elapsedNow()

  private var internalConnection: HttpConnect? = null

  override suspend fun makePostRequest(
    pool: ConnectionPoll,
    method: String,
    url: URL,
    headers: Headers,
  ): HttpRequestBody {
    created = TimeSource.Monotonic.markNow()
    val newKey = "${url.schema}://${url.host}"
    var channel = channel
    if (channel == null) {
      channel = networkManager.tcpConnect(
        DomainSocketAddress(
          host = url.domain,
          port = url.port ?: url.getPort(),
        ).resolve(),
      ).also { it.channel.setTcpNoDelay(true) }
      this.channel = channel
    }
    val sslSession = sslContext.clientSession(host = url.domain, port = url.port ?: url.getPort())
    val sslChannel = sslSession.asyncChannel(channel = channel, closeParent = true, bufferSize = sslBufferSize)
    val newUrl = when (url.schema) {
      "https" -> url.copy(schema = "http")
      "wss" -> url.copy(schema = "ws")
      else -> url
    }

    var internalConnection = internalConnection
    if (internalConnection == null) {
      internalConnection = protocolSelector.select(newUrl).createConnect(sslChannel)
      this.internalConnection = internalConnection
    }
    return internalConnection.makePostRequest(
      pool = { key, connection ->
        closed = !connection.isAlive
        pool.recycle(newKey, this)
      },
      method = method,
      url = url,
      headers = headers,
    )
  }

  override suspend fun asyncClose() {
    channel?.asyncClose()
  }
}
