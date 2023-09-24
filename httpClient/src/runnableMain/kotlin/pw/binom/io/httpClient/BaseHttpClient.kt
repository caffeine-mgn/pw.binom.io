package pw.binom.io.httpClient

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.Environment
import pw.binom.io.ByteBufferFactory
import pw.binom.io.http.Encoding
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.os
import pw.binom.pool.FixedSizePool
import pw.binom.url.URL

class BaseHttpClient(
  val useKeepAlive: Boolean = true,
  val bufferSize: Int = DEFAULT_BUFFER_SIZE,
  bufferCapacity: Int = 16,
  websocketMessagePoolSize: Int = 16,
  val requestHook: RequestHook = RequestHook.Default,
  val protocolSelector: ProtocolSelector,
) : AbstractHttpClient() {

  init {
    HttpMetrics.baseHttpClientCountMetric.inc()
  }

  internal val textBufferPool = FixedSizePool(
    capacity = bufferCapacity,
    manager = ByteBufferFactory(bufferSize),
  )

  private val poolConnectFactory = HttpConnectionPool.Factory { pool, uri ->
    protocolSelector.select(uri).createConnect()
  }

  internal val connectionPool = DefaultHttpConnectionPool()

  override suspend fun startConnect(
    method: String,
    uri: URL,
    headers: Headers,
    requestLength: OutputLength,
    keepAlive: Boolean?,
  ): HttpRequestBody {
    require(requestLength !is OutputLength.Chunked || headers.transferEncoding == null || headers.transferEncoding == Encoding.CHUNKED)
    require(requestLength !is OutputLength.Fixed || headers.contentLength == null || headers.contentLength!!.toLong() == requestLength.length)

    val newHeaders = HashHeaders2(headers)
    if (requestLength is OutputLength.Chunked) {
      newHeaders.transferEncoding = Encoding.CHUNKED
    }
    if (requestLength is OutputLength.Fixed) {
      newHeaders.transferEncoding = null
      newHeaders.contentLength = requestLength.length.toULong()
    }
    newHeaders.keepAlive = keepAlive ?: useKeepAlive

    val realUrl = requestHook.connectAddress(uri)

    val connection = connectionPool.borrow(url = realUrl, factory = poolConnectFactory)

    return connection.makePostRequest(
      pool = { key, connection ->
        if (connection.isAlive) {
          connectionPool.recycle(realUrl, connection)
        } else {
          connection.asyncCloseAnyway()
        }
      },
      method = method,
      url = uri,
      headers = headers,
    )
  }

  override suspend fun connect(method: String, uri: URL): HttpRequest {
    val req = HttpRequestImpl2(
      client = this,
      method = method,
      url = uri,
    )
    req.headers[Headers.USER_AGENT] = "binom-kotlin-${KotlinVersion.CURRENT} os/${Environment.os.name.lowercase()}"
    req.headers[Headers.HOST] = uri.host + (uri.port?.let { ":$it" } ?: "")
    return req
  }

  override fun close() {
    HttpMetrics.baseHttpClientCountMetric.dec()
//        deadlineTimer.close()
    GlobalScope.launch { connectionPool.asyncClose() }
  }
}

internal fun URL.getPort() =
  port ?: when (schema) {
    "ws", "http" -> 80
    "wss", "https" -> 443
    "ssh" -> 22
    "ftp" -> 21
    "rdp" -> 3389
    "vnc" -> 5900
    "telnet" -> 23
    else -> throw IllegalArgumentException("Unknown default port for $this")
  }
