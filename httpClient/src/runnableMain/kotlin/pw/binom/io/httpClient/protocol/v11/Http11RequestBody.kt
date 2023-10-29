package pw.binom.io.httpClient.protocol.v11

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.AbstractHttpRequestBody
import pw.binom.io.httpClient.HttpResponse
import pw.binom.url.URL

class Http11RequestBody(
  val url: URL,
  override val headers: Headers,
  override val autoFlushBuffer: Int,
  override val input: AsyncInput,
  override val output: AsyncOutput,
  private val requestFinishedListener: RequestFinishedListener? = null,
) : AbstractHttpRequestBody() {

  private var flushed = false
  private var resp: Http11Response? = null

  override suspend fun flush(): HttpResponse {
    val resp2 = resp
    if (resp2 != null) {
      return resp2
    }
    check(!flushed) { "Already flushed" }
    flushed = true
    val bufferedInput = input.bufferedAsciiReader(closeParent = false)
    val resp = Http11ConnectFactory2.readResponse(bufferedInput)
    val resultInput = Http11ConnectFactory2.prepareHttpResponse(
      stream = bufferedInput,
      contentLength = resp.headers.contentLength?.toLong(),
      contentEncoding = resp.headers.getContentEncodingList(),
      transferEncoding = resp.headers.getTransferEncodingList(),
    )

    val resp3 = Http11Response(
      resp = resp,
      inputStream = resultInput,
      requestFinishedListener = requestFinishedListener,
      defaultKeepAlive = resp.version == Http11ConnectFactory2.Http1Version.V1_1,
      url = url,
    )
    this.resp = resp3
    return resp3
  }

  override suspend fun asyncClose() {
    if (flushed) {
      return
    }
    flushed = true
    requestFinishedListener?.requestFinished(
      responseKeepAlive = false,
      success = false,
    )
  }
}
