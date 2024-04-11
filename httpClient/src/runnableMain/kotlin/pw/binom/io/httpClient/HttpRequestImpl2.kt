package pw.binom.io.httpClient

import pw.binom.atomic.AtomicBoolean
import pw.binom.charset.Charsets
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.http.Encoding
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.MutableHeaders
import pw.binom.url.URL

class HttpRequestImpl2(val client: BaseHttpClient, override val method: String, override val url: URL) : HttpRequest {
  override val headers: MutableHeaders = HashHeaders2()
  override var request: String = url.request.ifEmpty { "/" }
  private var hasBodyExist: Boolean = false

  private var httpRequestBody: HttpRequestBody? = null

  private suspend fun makeRequest(): HttpRequestBody {
    check(httpRequestBody == null) { "Request already sent" }
    val requestLength =
      if (headers.contentLength != null || headers.transferEncoding != Encoding.CHUNKED) {
        OutputLength.None
      } else {
        OutputLength.Chunked
      }
    val req =
      client.startConnect(
        method = method,
        uri = url,
        headers = headers,
        requestLength = requestLength,
      )
    httpRequestBody = req
    return req
  }

  override suspend fun writeBinary(): AsyncHttpRequestOutput {
    var req: HttpRequestBody? = null
    var output: AsyncOutput? = null

    suspend fun req(): HttpRequestBody {
      var reqInternal = req
      return if (reqInternal == null) {
        val bodyDefined =
          headers.contentLength != null

        if (!bodyDefined) {
          headers.transferEncoding = Encoding.CHUNKED
        }

        reqInternal = makeRequest()
        req = reqInternal
        reqInternal
      } else {
        reqInternal
      }
    }

    suspend fun output(): AsyncOutput {
      var o = output
      if (o != null) {
        return o
      }
      o = req().startWriteBinary()
      output = o
      return o
    }
    req()
    return object : AsyncHttpRequestOutput {
      private val closed = AtomicBoolean(false)

      override suspend fun getInput(): HttpResponse {
        if (hasBodyExist) {
          output().asyncClose()
        }
        return req().flush()
      }

      override suspend fun write(data: ByteBuffer): Int {
        val len = output().write(data)
        if (len > 0) {
          hasBodyExist = true
        }
        return len
      }

      override suspend fun asyncClose() {
        if (closed.compareAndSet(false, true)) {
          flush()
          req().asyncClose()
        }
      }

      override suspend fun flush() {
        if (hasBodyExist) {
          output().flush()
        }
      }
    }
  }

  override suspend fun writeText(): AsyncHttpRequestWriter {
    val dataChannel = writeBinary()
    return RequestAsyncHttpRequestWriter(
      output = dataChannel,
      bufferSize = client.bufferSize,
      charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8,
    )
  }

  override suspend fun getResponse(): HttpResponse {
    val httpRequestBody = httpRequestBody
    if (httpRequestBody != null) {
      return httpRequestBody.flush()
    }
    val req = makeRequest()
    return req.flush()
  }

  override suspend fun asyncClose() {
    httpRequestBody?.asyncClose()
  }
}
