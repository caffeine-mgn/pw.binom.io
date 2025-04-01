package pw.binom.http.client

import org.w3c.xhr.XMLHttpRequest
import pw.binom.io.ClosedException
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers

class HttpClientJsExchange(val xhr: XMLHttpRequest) : HttpClientCommonExchange {
  init {
    require(xhr.readyState >= XMLHttpRequest.OPENED) { "Invalid state of XMLHttpRequest" }
  }

  private var sent = false
  private var closed = false
  private var responseCore = -1
  private var headers: Headers? = null

  private fun ensureOpen() {
    if (closed) {
      throw ClosedException("Exchange already closed")
    }
  }

  private fun sendIfNeed() {
    if (!sent) {
      sent = true
      xhr.send()
    }
  }

  override suspend fun getResponseHeaders(): Headers {
    this.headers?.let { return it }
    ensureOpen()
    sendIfNeed()
    xhr.waitReadyState(XMLHttpRequest.HEADERS_RECEIVED)
    val headers = HashHeaders2()
    xhr.getAllResponseHeaders().lineSequence().filter { it.isEmpty() }.map {
      val items = it.split(':', limit = 2)
      val key = items[0]
      val value = items.getOrNull(1)?.removePrefix(" ") ?: ""
      headers.add(key, value)
    }
    this.headers = headers
    return headers
  }

  override suspend fun getResponseCode(): Int {
    if (responseCore >= 0) {
      return responseCore
    }
    ensureOpen()
    sendIfNeed()
    xhr.waitReadyState(XMLHttpRequest.HEADERS_RECEIVED)
    responseCore = xhr.status.toInt()
    return responseCore
  }

  override suspend fun readAllText(): String {
    ensureOpen()
    sendIfNeed()
    xhr.waitReadyState(XMLHttpRequest.DONE)
    return xhr.responseText
  }

  override suspend fun readAllBytes(): ByteArray {
    ensureOpen()
    sendIfNeed()
    xhr.waitReadyState(XMLHttpRequest.DONE)
    return xhr.responseText.encodeToByteArray()
  }

  override suspend fun sendText(text: String) {
    ensureOpen()
    check(!sent)
    check(xhr.readyState <= XMLHttpRequest.HEADERS_RECEIVED)
    sent = true
    xhr.send(text)
  }

  override suspend fun asyncClose() {
    if (closed) {
      return
    }
    closed = true
    if (xhr.readyState <= XMLHttpRequest.LOADING) {
      xhr.abort()
    }
  }
}
