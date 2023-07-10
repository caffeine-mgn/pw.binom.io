package pw.binom.io.httpClient.protocol.httpproxy

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpAuth
import pw.binom.io.httpClient.HttpRequestBody
import pw.binom.io.httpClient.getPort
import pw.binom.io.httpClient.protocol.ConnectionPoll
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2
import pw.binom.io.httpClient.protocol.v11.Http11RequestBody
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.socket.UnknownHostException
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.url.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class HttpProxyConnect(
  val proxyUrl: NetworkAddress,
  private val networkManager: NetworkManager,
  private var tcp: AsyncChannel?,
  private val protocolSelector: ProtocolSelector,
  protected val auth: HttpAuth?,
) : HttpConnect {
  private var created = TimeSource.Monotonic.markNow()
  private var transparentChannel: HttpConnect? = null
  private var closed = false

  override val isAlive: Boolean
    get() = !closed

  override val age: Duration
    get() = created.elapsedNow()

  private suspend fun getTcpConnect(): AsyncChannel {
    var tcp = tcp
    if (tcp == null) {
      tcp = networkManager.tcpConnect(
        InetNetworkAddress.create(
          host = proxyUrl.host,
          port = proxyUrl.port,
        ),
      )
      this.tcp = tcp
    }
    return tcp
  }

  private suspend fun makeHttpPostRequest(
    pool: ConnectionPoll,
    method: String,
    url: URL,
    headers: Headers,
  ): HttpRequestBody {
    val newKey = "http://${proxyUrl.host}:${proxyUrl.port}"
    val tcp = getTcpConnect()
    val output = tcp // .bufferedAsciiWriter(closeParent = false)
    val input = tcp // .bufferedAsciiReader(closeParent = false)
    val newHeaders = HashHeaders2(headers)
    newHeaders.remove(Headers.CONNECTION)
    if (auth != null) {
      newHeaders[Headers.PROXY_AUTHORIZATION] = auth.headerValue
    }
//        val headersWithoutKeepAlive = if (headers.containsKey(Headers.CONNECTION)) {
//            val newHeaders = HashHeaders2(headers)
//            newHeaders.remove(Headers.CONNECTION)
//            newHeaders
//        } else {
//            headers
//        }
    output.bufferedAsciiWriter(closeParent = false).use { writer ->
      Http11ConnectFactory2.sendRequest(
        output = writer,
        method = method,
        request = url.toString(),
        headers = newHeaders,
      )
    }
    output.flush()

    return Http11RequestBody(
      headers = headers,
      autoFlushBuffer = DEFAULT_BUFFER_SIZE,
      input = input,
      output = output,
      requestFinishedListener = { responseKeepAlive, success ->
        pool.recycle(key = newKey, connect = this)
      },
    )
  }

  private suspend fun makeConnect(
    pool: ConnectionPoll,
    method: String,
    url: URL,
    headers: Headers,
  ): HttpRequestBody {
    val newKey = "${url.schema}://${url.host}${url.port?.let { ":$it" } ?: ""}"
    val tcp = getTcpConnect()
    val output = tcp // .bufferedAsciiWriter(closeParent = false)
    val input = tcp.bufferedAsciiReader(closeParent = false)
    val host = "${url.host}:${url.port ?: url.getPort()}"
    var transparentChannel = transparentChannel
    if (transparentChannel == null) {
      val newHeaders = HashHeaders2()
      if (auth != null) {
        newHeaders[Headers.PROXY_AUTHORIZATION] = auth.headerValue
      }
      newHeaders[Headers.PROXY_CONNECTION] = Headers.KEEP_ALIVE
      newHeaders[Headers.HOST] = host
      output.bufferedAsciiWriter(closeParent = false).use { bufOutput ->
        Http11ConnectFactory2.sendRequest(
          output = bufOutput,
          method = "CONNECT",
          request = host,
          headers = newHeaders,
        )
      }
      output.flush()
      val resp = Http11ConnectFactory2.readResponse(input)
      if (resp.responseCode == 404) {
        throw UnknownHostException(url.host)
      }
      if (resp.responseCode != 200) {
        throw IOException("Invalid response code: ${resp.responseCode}")
      }
      val channel = AsyncChannel.create(
        input = input,
        output = output,
      )
      val factory = protocolSelector.select(url = url)
      transparentChannel = factory.createConnect(channel)
      this.transparentChannel = transparentChannel
    }

    return transparentChannel.makePostRequest(
      pool = { key, connect ->
        closed = !connect.isAlive
        pool.recycle(key = newKey, connect = this)
      },
      method = method,
      url = url,
      headers = headers,
    )
  }

  override suspend fun makePostRequest(
    pool: ConnectionPoll,
    method: String,
    url: URL,
    headers: Headers,
  ): HttpRequestBody {
    created = TimeSource.Monotonic.markNow()
    return when {
      url.schema == "http" && headers[Headers.UPGRADE] == null -> makeHttpPostRequest(
        pool = pool,
        method = method,
        url = url,
        headers = headers,
      )

      else -> makeConnect(
        pool = pool,
        method = method,
        url = url,
        headers = headers,
      )
    }
  }

  override suspend fun asyncClose() {
    closed = true
    transparentChannel?.asyncClose()
    tcp?.asyncClose()
  }
}
