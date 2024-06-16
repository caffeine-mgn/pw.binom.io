package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.atomic.AtomicBoolean
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.*
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.io.http.AsyncEmptyHttpInput
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.http.websocket.WebSocketConnectionImpl
import pw.binom.pool.using
import pw.binom.url.Path
import pw.binom.url.Query
import kotlin.time.Duration

@Deprecated(message = "Use HttpServer2")
internal class HttpRequest3Impl(
  override val request: String,
  override val method: String,
  override val headers: Headers,
  private val channel: ServerAsyncAsciiChannel,
  val textBufferPool: ByteBufferPool,
  val compressBufferPool: ByteBufferPool,
  val returnToIdle: IdlePool?,
  val keepAliveEnabled: Boolean,
  val charBufferSize: Int,
) : HttpRequest {
  companion object {
    suspend fun read(
      channel: ServerAsyncAsciiChannel,
      server: HttpServer,
//            isNewConnect: Boolean,
      readStartTimeout: Duration,
//            idleJob: Job?,
      returnToIdle: IdlePool?,
    ): Result<HttpRequest3Impl?> =
      runCatching {
        val request =
          if (readStartTimeout.isPositive() && readStartTimeout.isFinite()) {
            server.waitTimeout(readStartTimeout)
            try {
              val line = channel.reader.readln()
              server.timeoutFinished()
              line
            } catch (e: Throwable) {
              if (e.cause != null && e.cause !is HttpReadTimeoutException) {
                server.timeoutFinished()
              }
              throw e
            }
          } else {
            channel.reader.readln()
          }

//            val request = if (readStartTimeout.isInfinite() || readStartTimeout == Duration.ZERO) {
//                channel.reader.readln()
//            } else {
//                withTimeoutOrNull(readStartTimeout) { channel.reader.readln() }
//            }

//            server.idleJobsLock.synchronize {
//                if (!server.idleJobs.remove(idleJob)) {
//                    return@runCatching null
//                }
//            }
        if (request == null) {
          return@runCatching null
        }

        val method: String
        val path: String
        HttpServerUtils.parseHttpRequest(request) { m, p ->
          method = m
          path = p
        }

        val headers = HttpServerUtils.readHeaders(channel = channel)

        HttpRequest3Impl(
          request = path,
          method = method,
          headers = headers,
          channel = channel,
          textBufferPool = server.textBufferPool,
          returnToIdle = returnToIdle,
          keepAliveEnabled = server.maxIdleTime.isPositive(),
          compressBufferPool = server.compressBufferPool,
          charBufferSize = 512,
        )
      }
  }

  private val closed = AtomicBoolean(false)

  override val path: Path = HttpServerUtils.extractPathFromRequest(request)
  override val query: Query? = HttpServerUtils.extractQueryFromRequest(request)

  private fun checkClosed() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }

  private var bodyReading = false
  private var readInput: AsyncInput? = null

  override fun readBinary(): AsyncInput {
    checkClosed()
    if (bodyReading) {
      throw IllegalStateException("Body already started reading")
    }
    if (!headers.bodyExist) {
      bodyReading = true
      readInput = AsyncEmptyHttpInput
      return AsyncEmptyHttpInput
    }
    val contentLength = headers.contentLength
    val transferEncoding = headers.getTransferEncodingList()
    var stream: AsyncInput = channel.reader
    if (contentLength != null) {
      stream =
        AsyncContentLengthInput(
          stream = stream,
          contentLength = contentLength,
          closeStream = false,
        )
    }

    for (i in transferEncoding.lastIndex downTo 0) {
      val newStream =
        HttpServerUtils.wrapStream(encoding = transferEncoding[i], stream = stream, closeStream = false)
          ?: throw IOException("Not supported encoding \"${transferEncoding[i]}\"")
      stream = newStream
    }
    bodyReading = true
    readInput = stream
    return stream
  }

  private suspend fun checkWebSocket() {
    val connection = headers[Headers.CONNECTION]
    if (connection == null) {
      sendReject()
      throw IllegalStateException("Invalid Client Headers: Missing Header \"${Headers.CONNECTION}\"")
    }
    if (!hasUpgrade()) {
      sendReject()
      throw IllegalStateException("Invalid Client Headers: Header \"${Headers.CONNECTION}: ${Headers.UPGRADE}\" not found")
    }
    if (!headers[Headers.UPGRADE]?.singleOrNull().equals(Headers.WEBSOCKET, true)) {
      sendReject()
      throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\"")
    }
  }

  override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection {
    checkClosed()
    checkWebSocket()
    val key = headers.getSingleOrNull(Headers.SEC_WEBSOCKET_KEY)
    if (key == null) {
      sendReject()
      throw IllegalStateException("Invalid Client Headers: Missing Header \"${Headers.SEC_WEBSOCKET_KEY}\"")
    }
    val sha1 = Sha1MessageDigest()
    val resp = response() as HttpResponse3Impl
    resp.status = 101
    resp.headers[Headers.CONNECTION] = Headers.UPGRADE
    resp.headers[Headers.UPGRADE] = Headers.WEBSOCKET
    resp.headers[Headers.SEC_WEBSOCKET_ACCEPT] = HandshakeSecret.generateResponse(sha1, key)
    resp.sendHeaders()
    return WebSocketConnectionImpl(
      _output = channel.writer,
      _input = channel.reader,
      masking = masking,
      mainChannel = resp.channel,
    )
//        return server.webSocketConnectionPool.new(
//            input = channel.reader,
//            output = channel.writer,
//            masking = masking,
//        )
  }

  override suspend fun acceptTcp(): AsyncChannel {
    checkClosed()
    checkTcp()
    val resp = response() as HttpResponse3Impl
    resp.status = 101
    resp.headers[Headers.CONNECTION] = Headers.UPGRADE
    resp.headers[Headers.UPGRADE] = Headers.TCP
    resp.sendHeaders()
    closed.setValue(true)
    return channel.channel
  }

  private fun hasUpgrade() =
    headers[Headers.CONNECTION]
      ?.asSequence()
      ?.flatMap { it.splitToSequence(',') }
      ?.map { it.trim() }
      ?.any { it.equals(Headers.UPGRADE, true) }
      ?: false

  private suspend fun checkTcp() {
    if (!hasUpgrade()) {
      sendReject()
      throw IllegalStateException("Invalid Client Headers: Header \"${Headers.CONNECTION}: ${Headers.UPGRADE}\" not found")
    }

    if (!headers[Headers.UPGRADE]?.singleOrNull().equals(Headers.TCP, true)) {
      sendReject()
      throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\"")
    }
  }

  override suspend fun response(): HttpResponse {
    checkClosed()
    if (startedResponse != null) {
      throw IllegalStateException("Response already got")
    }
//        val server = server ?: throw SocketClosedException()
    val inputForSkip = readInput ?: readBinary()
    textBufferPool.using { buf ->
      inputForSkip.useAsync {
        it.skipAll(buf)
      }
    }
    val r =
      HttpResponse3Impl(
        keepAliveEnabled = keepAliveEnabled && (headers.keepAlive ?: true),
        channel = channel,
        acceptEncoding = headers.acceptEncoding,
        returnToIdle = returnToIdle,
        textBufferPool = textBufferPool,
        compressBufferPool = compressBufferPool,
        charBufferSize = charBufferSize,
      )
//        val r = server.httpResponse2Impl.borrow().also {
//            it.reset(
//                keepAliveEnabled = server.maxIdleTime.isPositive() && headers.keepAlive,
//                channel = channel,
//                acceptEncoding = headers.acceptEncoding,
//                server = server,
//                onclosed = { }
//            )
//        }
    startedResponse = r
    closed.setValue(true)
    return r
  }

  override suspend fun <T> response(func: suspend (HttpResponse) -> T): T {
    return super.response(func)
  }

  private var startedResponse: HttpResponse? = null

  override val response: HttpResponse?
    get() = startedResponse
  override val isReadyForResponse: Boolean
    get() = !closed.getValue() && startedResponse == null

  override suspend fun asyncClose() {
    checkClosed()
    response {
      it.status = 404
    }
  }
}
