package pw.binom.http.client

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.EventTarget
import org.w3c.xhr.XMLHttpRequest
import pw.binom.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


internal fun XMLHttpRequest.clearEvents() {
  onreadystatechange = null
  onerror = null
  onabort = null
}

internal fun EventTarget.eventListenerOnce(event: String, listener: (Event) -> Unit): AutoCloseable {
  var func: EventListener? = null
  func = EventListener {
    listener(it)
    removeEventListener(event, func!!)
  }
  addEventListener(event, func)
  return AutoCloseable {
    removeEventListener(event, func)
  }
}

internal suspend fun XMLHttpRequest.waitReadyState(state: Short) {
  if (readyState >= state) {
    return
  }
  suspendCancellableCoroutine { con ->
    var stateListener: AutoCloseable? = null
    var abortListener: AutoCloseable? = null
    var errorListener: AutoCloseable? = null
    fun clearEvents() {
      stateListener?.close()
      abortListener?.close()
      errorListener?.close()
    }
    stateListener = eventListenerOnce("readystatechange") {
      if (readyState >= state) {
        clearEvents()
        con.resume(Unit)
      }
    }
    abortListener = eventListenerOnce("abort") {
      clearEvents()
      con.resumeWithException(CancellationException())
    }
    errorListener = eventListenerOnce("error") {
      clearEvents()
      con.resumeWithException(IOException())
    }
  }
}
