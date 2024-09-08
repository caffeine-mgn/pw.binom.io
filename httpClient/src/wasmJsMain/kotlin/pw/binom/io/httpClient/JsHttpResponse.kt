package pw.binom.io.httpClient

import org.w3c.xhr.XMLHttpRequest
import pw.binom.asyncInput
import pw.binom.io.*
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.url.URL

class JsHttpResponse internal constructor(val url: URL, val xhr: XMLHttpRequest) : HttpResponse {
  override val responseCode: Int
    get() = xhr.status.toInt()

  override val path
    get() = url.path

  override val query
    get() = url.query

  override val inputHeaders: Headers by lazy {
    val r = HashHeaders2()
    xhr.getAllResponseHeaders().split("\r\n").forEach {
      if (it.isEmpty()) {
        return@forEach
      }
      val items = it.split(": ")
      r.add(items[0], items[1])
    }
    r
  }

  override suspend fun readBinary(): AsyncInput {
    return ByteArrayInput(ByteArray(xhr.responseText.length) {
      xhr.responseText[it].code.toByte()
    }).asyncInput()
  }

  override suspend fun readText(): AsyncReader = readAllText().asReader().asAsync()

  override suspend fun readAllText(): String {
    return xhr.responseText
  }

  override suspend fun asyncClose() {
  }
}
