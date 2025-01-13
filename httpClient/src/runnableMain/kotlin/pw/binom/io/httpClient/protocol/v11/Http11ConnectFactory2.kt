package pw.binom.io.httpClient.protocol.v11

import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.httpClient.ConnectionFactory
import pw.binom.io.httpClient.protocol.ConnectFactory2
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.network.NetworkManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Http11ConnectFactory2(
  val networkManager: NetworkManager,
  val connectFactory: ConnectionFactory,
  val defaultKeepAliveTimeout: Duration = 5.seconds,
) : ConnectFactory2 {
  enum class Http1Version {
    V1_1, V1_0,
  }

  class Response(val headers: Headers, val responseCode: Int, val version: Http1Version)
  companion object {

    suspend fun readResponse(input: AsyncReader): Response {
      val httpVersion: Http1Version
      val responseCode: Int
      val headers = HashHeaders()
      Http11.readResponse(
        input = input,
        httpVersion = { version ->
          httpVersion = when (version) {
            "HTTP/1.1" -> Http1Version.V1_1
            "HTTP/1.0" -> Http1Version.V1_0
            else -> throw IOException("Unsupported HTTP version. Response: \"$version\"")
          }
        },
        responseCode = {
          responseCode = it
        },
        header = { key, value ->
          headers.add(key = key, value = value)
        }
      )
      return Response(
        headers = headers,
        responseCode = responseCode,
        version = httpVersion,
      )
    }

    fun prepareHttpResponse(
      stream: AsyncInput,
      contentLength: Long?,
      contentEncoding: List<String>,
      transferEncoding: List<String>,
    ): AsyncInput {
      var stream: AsyncInput = stream
      if (contentLength != null) {
        stream = AsyncContentLengthInput(
          stream = stream,
          contentLength = contentLength.toULong(),
          closeStream = false,
        )
      }

      fun wrap(encode: String, stream: AsyncInput) = when (encode) {
        Encoding.CHUNKED -> AsyncChunkedInput(
          stream = stream,
          closeStream = false,
        )

        Encoding.GZIP -> AsyncGZIPInput(stream, closeStream = true)
        Encoding.DEFLATE -> AsyncInflateInput(stream = stream, closeStream = true, wrap = true)
        Encoding.IDENTITY -> stream
        else -> null
      }
      for (i in transferEncoding.lastIndex downTo 0) {
        stream = wrap(encode = transferEncoding[i], stream = stream)
          ?: throw IOException("Unknown Content Encoding: \"${transferEncoding[i]}\"")
      }
      for (i in contentEncoding.lastIndex downTo 0) {
        stream = wrap(encode = contentEncoding[i], stream = stream)
          ?: throw IOException("Unknown Content Encoding: \"${contentEncoding[i]}\"")
      }
      return stream
    }
  }

  override fun createConnect() = Http11Connect(
    networkManager = networkManager,
    defaultKeepAliveTimeout = defaultKeepAliveTimeout,
    tcp = null,
    connectFactory = connectFactory,
  )

  override fun createConnect(channel: AsyncChannel): HttpConnect = Http11Connect(
    networkManager = networkManager,
    defaultKeepAliveTimeout = defaultKeepAliveTimeout,
    tcp = channel,
    connectFactory = connectFactory,
  )
}
