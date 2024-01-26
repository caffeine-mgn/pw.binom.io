@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.url.URI
import pw.binom.url.toPath

class HttpServerExchangeImpl(
  override val requestURI: URI,
  override val requestHeaders: Headers,
  override val requestMethod: String,
  val channel: ServerAsyncAsciiChannel,
  val keepAliveEnabled: Boolean,
  val compressByteBufferPool: ByteBufferPool?,
  val compressLevel: Int,
) : HttpServerExchange {
  override val mainChannel: AsyncCloseable =
    AsyncCloseable {
      channel.asyncCloseAnyway()
    }
  internal var keepAlive = false
  override val address: InetNetworkAddress
    get() = channel.address

  override suspend fun startResponse(
    statusCode: Int,
    headers: Headers,
  ) {
    check(!headersSent) { "Headers already sent" }
    val connection = headers.getLast(Headers.CONNECTION)
    if (connection == Headers.KEEP_ALIVE && !keepAliveEnabled) {
      throw IllegalArgumentException("KeepAlive is disabled")
    }
    var transferEncoding = headers.getTransferEncodingList()
    val contentEncoding = headers.getContentEncodingList()
    val contentLength = headers.contentLength
    val hasUpgrade = headers[Headers.UPGRADE] != null

    headersSent = true

    channel.writer.append("HTTP/1.1 ").append(statusCode.toString()).append(" ")
      .append(HttpServerUtils.statusCodeToDescription(statusCode))
      .append(Utils.CRLF)

    if (requestMethod == "CONNECT" && statusCode >= 200 && statusCode < 300) {
      require(!hasUpgrade) { "Connect method doesn't allow to use Upgrade" }
      require(contentLength == null) { "Connect method doesn't allow to ${Headers.CONTENT_LENGTH}" }
      require(contentEncoding.isEmpty()) { "Connect method doesn't allow to ContentEncoding" }
      require(transferEncoding.isEmpty()) { "Connect method doesn't allow to TransferEncoding" }
      channel.writer.append(Utils.CRLF)
      channel.writer.flush()
      this.outputStream = channel.channel
      this.inputStream = channel.channel
      return
    }

    headers.forEachHeader { key, value ->
      channel.writer.append(key).append(": ").append(value).append(Utils.CRLF)
    }
    val clientSupportKeepAlive = requestHeaders[Headers.CONNECTION]?.any { it == Headers.KEEP_ALIVE } == true
    val proxyClientSupportKeepAlive =
      requestHeaders[Headers.PROXY_CONNECTION]?.any { it == Headers.KEEP_ALIVE } == true
    if (connection == null && !hasUpgrade && (clientSupportKeepAlive || proxyClientSupportKeepAlive)) {
      if (keepAliveEnabled) {
        keepAlive = true
        if (clientSupportKeepAlive) {
          channel.writer.append(Headers.CONNECTION).append(": ").append(Headers.KEEP_ALIVE).append(Utils.CRLF)
        }
        if (proxyClientSupportKeepAlive) {
          channel.writer.append(Headers.PROXY_CONNECTION).append(": ").append(Headers.KEEP_ALIVE)
            .append(Utils.CRLF)
        }
      } else {
        channel.writer.append(Headers.CONNECTION).append(": ").append(Headers.CLOSE).append(Utils.CRLF)
      }
    }
    // empty any length of content
    if (contentLength == null && !transferEncoding.any { it == Encoding.CHUNKED } && !hasUpgrade) {
      channel.writer.append(Headers.TRANSFER_ENCODING).append(": ").append(Encoding.CHUNKED).append(Utils.CRLF)
      transferEncoding = listOf(Encoding.CHUNKED) + transferEncoding
    }
    channel.writer.append(Utils.CRLF)
    channel.writer.flush()
    var output: AsyncOutput = channel.channel
    if (contentLength != null) {
      output =
        AsyncContentLengthOutput(
          stream = output,
          contentLength = contentLength,
          closeStream = false,
        )
    }

    for (i in transferEncoding.lastIndex downTo 0) {
      val it = transferEncoding[i]
      output = HttpServerUtils.wrapStream(
        encoding = it,
        stream = output,
        closeStream = true,
        compressBufferPool = compressByteBufferPool,
        compressLevel = compressLevel,
      )
        ?: throw IOException("Not supported encoding \"$it\"")
    }

    for (i in contentEncoding.lastIndex downTo 0) {
      val it = contentEncoding[i]
      output = HttpServerUtils.wrapStream(
        encoding = it,
        stream = output,
        closeStream = true,
        compressBufferPool = compressByteBufferPool,
        compressLevel = compressLevel,
      )
        ?: throw IOException("Not supported encoding \"$it\"")
    }
    this.outputStream = output
  }

  internal var headersSent = false
    private set
  private var inputStream: AsyncInput? = null
  private var outputStream: AsyncOutput? = null

  override val input: AsyncInput
    get() {
      val inputStream = inputStream
      if (inputStream != null) {
        return inputStream
      }

      val contentLength = requestHeaders.contentLength
      val transferEncoding = requestHeaders.getTransferEncodingList()
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
      this.inputStream = stream
      return stream
    }
  override val output: AsyncOutput
    get() {
      check(headersSent) { "Output Stream access only after header send" }
      return outputStream ?: throw IllegalStateException("Output stream not ready")
    }
  override val responseStarted: Boolean
    get() = headersSent
  override val requestContext
    get() = "/".toPath

  private val queryParams2 by lazy { requestURI.query?.toMap() ?: emptyMap() }

  override fun getQueryParams() = queryParams2

  internal suspend fun finishRequest() {
    outputStream?.asyncCloseAnyway()
  }
}
