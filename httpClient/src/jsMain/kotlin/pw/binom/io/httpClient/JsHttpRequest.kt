package pw.binom.io.httpClient

import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncOutput
import pw.binom.io.AsyncWriter
import pw.binom.io.ByteBuffer
import pw.binom.io.http.Encoding
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.MutableHeaders
import pw.binom.url.URL

class JsHttpRequest(val client: JsBaseHttpClient, override val method: String, override val url: URL) : HttpRequest {
  override var request: String = url.request.ifEmpty { "/" }
  private var hasBodyExist = false

  private var httpRequestBody: HttpRequestBody? = null
  override val headers: MutableHeaders = HashHeaders2()

  private suspend fun makeRequest(): HttpRequestBody {
    val httpRequestBody = httpRequestBody
    if (httpRequestBody != null) {
      return httpRequestBody
    }
    val req = client.startConnect(
      method = method,
      uri = url,
      headers = headers,
      requestLength = if (headers.contentLength != null || headers.transferEncoding != Encoding.CHUNKED) OutputLength.None else OutputLength.Chunked,
    )
    this.httpRequestBody = req
    return req
  }

  override suspend fun writeBinary(): AsyncHttpRequestOutput {
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

        q = makeRequest()
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
    var req: HttpRequestBody? = null
    var output: AsyncWriter? = null
    suspend fun req(): HttpRequestBody {
      var reqInternal = req
      return if (reqInternal == null) {
        reqInternal = makeRequest()
        req = reqInternal
        reqInternal
      } else {
        reqInternal
      }
    }

    suspend fun output(): AsyncWriter {
      var o = output
      if (o != null) {
        return o
      }
      o = req().startWriteText()
      output = o
      return o
    }
    req()
    return object : AsyncHttpRequestWriter {
      override suspend fun getInput(): HttpResponse {
        if (hasBodyExist) {
          output().asyncClose()
        }
        return req().flush()
      }

      override suspend fun append(value: CharSequence?): AsyncAppendable {
        output().append(value)
        return this
      }

      override suspend fun append(value: Char): AsyncAppendable {
        output().append(value)
        return this
      }

      override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable {
        output().append(value = value, startIndex = startIndex, endIndex = endIndex)
        return this
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

  override suspend fun getResponse(): HttpResponse {
    val req = makeRequest()
    return req.flush()
  }

  override suspend fun asyncClose() {
    httpRequestBody?.asyncClose()
  }
}
