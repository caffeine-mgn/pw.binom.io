package pw.binom.http.client

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.xhr.TEXT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import pw.binom.io.IOException
import pw.binom.io.http.Headers
import pw.binom.io.http.forEachHeader
import pw.binom.io.httpClient.JsHttpResponse
import pw.binom.url.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpClientJs : HttpClientCommon {
  override suspend fun connect(method: String, url: URL, headers: Headers): HttpClientJsExchange {
    return suspendCancellableCoroutine { con ->
      val xhr = XMLHttpRequest()
      con.invokeOnCancellation {
        xhr.clearEvents()
        xhr.abort()
      }
      xhr.responseType = XMLHttpRequestResponseType.TEXT
      if (!headers.isNotEmpty()) {
        try {
          headers.forEachHeader { key, value ->
            xhr.setRequestHeader(key, value)
          }
        } catch (e: Throwable) {
          con.resumeWithException(e)
          return@suspendCancellableCoroutine
        }
      }
      xhr.onreadystatechange = { event ->
        if (xhr.readyState >= XMLHttpRequest.OPENED) {
          xhr.clearEvents()
          con.resume(HttpClientJsExchange(xhr = xhr))
        }
      }
      xhr.onerror = { event ->
        xhr.clearEvents()
        con.resumeWithException(IOException("Can't get $url"))
      }
      xhr.onabort = {
        xhr.clearEvents()
        con.resumeWithException(CancellationException("Fetching $url was aborted"))
      }
      xhr.open(method, url.toString())
    }
  }

  override suspend fun asyncClose() {
    TODO("Not yet implemented")
  }
}
