package pw.binom.io.httpClient

import pw.binom.charset.Charsets
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.http.Encoding
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.MutableHeaders
import pw.binom.url.URL

class HttpRequestImpl2(val client: BaseHttpClient, override val method: String, override val uri: URL) : HttpRequest {
  override val headers: MutableHeaders = HashHeaders2()
  override var request: String = uri.request.ifEmpty { "/" }
  private var hasBodyExist: Boolean = false

  private var httpRequestBody: HttpRequestBody? = null

  private suspend fun makeRequest(): HttpRequestBody {
    check(httpRequestBody == null)
    val req = client.startConnect(
      method = method,
      uri = uri,
      headers = headers,
      requestLength = if (headers.contentLength != null || headers.transferEncoding != Encoding.CHUNKED) OutputLength.None else OutputLength.Chunked,
    )
    httpRequestBody = req
    return req
  }

  override suspend fun writeData(): AsyncHttpRequestOutput {
    var req: HttpRequestBody? = null
    var output: AsyncOutput? = null
    suspend fun req(): HttpRequestBody {
      var q = req
      return if (q == null) {
        val bodyDefined = headers.contentLength != null &&
          headers.transferEncoding?.let { Encoding.CHUNKED in it } ?: false

        if (!bodyDefined) {
          headers.transferEncoding = Encoding.CHUNKED
        }

        println("HttpRequestImpl2:: sending headers...")
        q = makeRequest()
        println("HttpRequestImpl2:: was sent!")
        req = q
        q
      } else {
        q
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
      override suspend fun getResponse(): HttpResponse {
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
        flush()
        req().asyncClose()
      }

      override suspend fun flush() {
        if (hasBodyExist) {
          output().flush()
        }
      }
    }
  }

  override suspend fun writeText(): AsyncHttpRequestWriter {
    val dataChannel = writeData()
    return RequestAsyncHttpRequestWriter(
      output = dataChannel,
      bufferSize = client.bufferSize,
      charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8,
    )
  }

  override suspend fun getResponse(): HttpResponse {
    val req = makeRequest()
    return req.flush()
  }

  override suspend fun asyncClose() {
    httpRequestBody?.asyncClose()
  }
}
