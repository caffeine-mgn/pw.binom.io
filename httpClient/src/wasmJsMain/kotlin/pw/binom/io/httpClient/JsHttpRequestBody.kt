package pw.binom.io.httpClient

import kotlinx.coroutines.CancellationException
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Int8Array
import org.khronos.webgl.set
import org.w3c.files.Blob
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

  override suspend fun send(text: String): JsHttpResponse {
    return super.send(text) as JsHttpResponse
  }

  override suspend fun send(data: ByteBuffer): JsHttpResponse {
    return super.send(data) as JsHttpResponse
  }

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

  private var resp: JsHttpResponse? = null
  private val flashWaters = ArrayList<Continuation<JsHttpResponse>>()

  override suspend fun flush(): JsHttpResponse {
    val rr = resp
    if (rr != null) {
      return rr
    }
    if (isFlushed) {
      return suspendCoroutine {
        flashWaters.add(it)
      }
    }
    isFlushed = true
    val resp = suspendCoroutine { con ->
      val xhr = XMLHttpRequest()
      var resumed = false
      xhr.responseType = XMLHttpRequestResponseType.TEXT
      xhr.onreadystatechange = { event ->
        if (xhr.readyState == 4.toShort() && xhr.status != 0.toShort()) {
          val resp = JsHttpResponse(url = url, xhr = xhr)
          flashWaters.forEach {
            it.resume(resp)
          }
          flashWaters.clear()
          con.resume(resp)
        }
      }
      xhr.onerror = { event ->
        flashWaters.forEach {
          it.resumeWithException(IOException("Can't get $url"))
        }
        flashWaters.clear()
        con.resumeWithException(IOException("Can't get $url"))
      }
      xhr.onabort = {
        flashWaters.forEach {
          it.resumeWithException(CancellationException("Fetching $url was aborted"))
        }
        flashWaters.clear()
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
          val out = Int8Array(it.array.size)
          it.array.forEachIndexed { index, byte ->
            out[index] = byte
          }
          val arr = JsArray<JsAny?>()
          arr[0] = out.buffer
          xhr.send(Blob(arr))
        }

        else -> xhr.send()
      }
    }
    this.resp = resp
    return resp
  }

  override val mainChannel: AsyncChannel
    get() = AsyncChannel.EMPTY

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
