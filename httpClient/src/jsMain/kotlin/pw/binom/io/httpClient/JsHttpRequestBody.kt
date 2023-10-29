package pw.binom.io.httpClient

import kotlinx.coroutines.CancellationException
import org.w3c.xhr.TEXT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import pw.binom.asyncOutput
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.http.forEachHeader
import pw.binom.url.URL
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class JsHttpRequestBody internal constructor(
  val method: String,
  val url: URL,
  val headers: Headers,
) : HttpRequestBody {
  override var isFlushed: Boolean = false
    private set
  override val isOutputStarted: Boolean
    get() = text != null || bytes != null || isFlushed
  override val input: AsyncInput
    get() = TODO("Not yet implemented")
  override val output: AsyncOutput
    get() = TODO("Not yet implemented")

  private var bytes: ByteArrayOutput? = null
  private var text: StringAsyncWriter? = null

  override suspend fun startWriteBinary(): AsyncOutput {
    check(text == null) { "Request already started" }
    check(bytes == null) { "Request already started" }
    var bytes = bytes
    bytes = ByteArrayOutput()
    this.bytes = bytes
    return bytes.asyncOutput(callClose = false)
  }

  override suspend fun startWriteText(): AsyncWriter {
    check(text == null) { "Request already started" }
    check(bytes == null) { "Request already started" }
    val text = StringAsyncWriter()
    this.text = text
    return text
  }

  private var resp: HttpResponse? = null
  private val flashWaters = arrayOf<Continuation<HttpResponse>>()

  override suspend fun flush(): HttpResponse {
    val rr = resp
    if (rr != null) {
      return rr
    }
    if (isFlushed) {
      return suspendCoroutine {
        flashWaters.asDynamic().push(it)
      }
    }
    isFlushed = true
    val resp = suspendCoroutine { con ->
      val xhr = XMLHttpRequest()
      xhr.responseType = XMLHttpRequestResponseType.TEXT
      xhr.onreadystatechange = {
        if (xhr.readyState == 4.toShort()) {
          val resp = JsHttpResponse(url = url, xhr = xhr)
          flashWaters.forEach {
            it.resume(resp)
          }
          flashWaters.asDynamic().length = 0
          con.resume(resp)
        }
      }
      xhr.onerror = {
        flashWaters.forEach {
          it.resumeWithException(IOException("Can't get $url"))
        }
        flashWaters.asDynamic().length = 0
        con.resumeWithException(IOException("Can't get $url"))
      }
      xhr.onabort = {
        flashWaters.forEach {
          it.resumeWithException(CancellationException("Fetching $url was aborted"))
        }
        flashWaters.asDynamic().length = 0
        con.resumeWithException(CancellationException("Fetching $url was aborted"))
      }
      val text = text
      val bytes = bytes
      xhr.open(method, url.toString())
      headers.forEachHeader { key, value ->
        xhr.setRequestHeader(key, value)
      }
      when {
        text != null -> xhr.send(text.toString())
        bytes != null -> bytes.locked {
          xhr.send(it.native.toInt8Array(startIndex = 0, endIndex = bytes.size))
        }

        else -> xhr.send()
      }
    }
    this.resp = resp
    return resp
  }

  override suspend fun asyncClose() {
  }

  private class StringAsyncWriter : AsyncWriter {
    private val sb = StringBuilder()
    override suspend fun append(value: CharSequence?): AsyncAppendable {
      value ?: return this
      sb.append(value)
      return this
    }

    override suspend fun append(value: Char): AsyncAppendable {
      sb.append(value)
      return this
    }

    override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable {
      value ?: return this
      return if (startIndex == 0 && endIndex == value.length) {
        append(value)
      } else {
        append(value.substring(startIndex = startIndex, endIndex = endIndex))
      }
    }

    override suspend fun flush() {

    }

    override suspend fun asyncClose() {

    }

    override fun toString(): String = sb.toString()
  }
}
