package pw.binom.http.client

import kotlinx.coroutines.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.LinkedList
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.DateTime
import pw.binom.http.client.factory.HttpConnectionFactory
import pw.binom.http.client.factory.NetSocketFactory
import pw.binom.io.http.Headers
import pw.binom.url.URL
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class HttpClientRunnable(
  private val factory: HttpConnectionFactory,
  private val idleTimeout: Duration = 1.minutes,
  private val source: NetSocketFactory,
  idleCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : HttpClientCommon {
  private val connections = HashMap<String, LinkedList<HttpConnection>>()
  private val lock = SpinLock()
  private val closed = AtomicBoolean(false)
  private val thread = if (idleTimeout.isInfinite() || idleTimeout.isNegative()) {
    null
  } else {
    GlobalScope.launch(idleCoroutineContext) {
      while (isActive) {
        try {
          delay(idleTimeout)
        } catch (e: CancellationException) {
          break
        }
        try {
          val now = DateTime.now
          lock.synchronize {
            val l = LinkedList<HttpConnection>()
            connections.removeFromListIf { s, httpConnection ->
              if (now - httpConnection.lastActive > idleTimeout) {
                l += httpConnection
                true
              } else {
                false
              }
            }
            l
          }.forEach {
            it.asyncCloseAnyway()
          }
        } catch (e: Throwable) {
          e.printStackTrace()
          break
        }
      }
    }
  }


  override suspend fun connect(method: String, url: URL, headers: Headers): HttpClientExchange {
    val cacheKey = url.let { "${it.schema}://${it.host}:${it.port}" }
    var existConnection = lock.synchronize {
      connections.removeLastOrNull(cacheKey)
    }
    if (existConnection == null) {
      existConnection = factory.connect(url = url, source = source) { con ->
        lock.synchronize {
          if (!closed.getValue()) {
            connections.addFirst(cacheKey, con)
          } else {
            con.asyncClose()
          }
        }
      }
    }
    return existConnection.connect(
      method = method,
      request = url.request,
      headers = headers,
    )
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    thread?.cancel()
    lock.synchronize {
      val l = connections.asSequence().flatMap { it.value.asSequence() }.toList()
      connections.clear()
      l
    }.forEach {
      it.asyncClose()
    }
  }

  override fun request(method: String, url: URL, headers: Headers): HttpRequestBuilder {
    val builder = super.request(method, url, headers)
    builder.headers.add(Headers.CONNECTION, Headers.KEEP_ALIVE)
    builder.headers.add(Headers.HOST, url.domain)
    builder.headers.add(Headers.SERVER, "binom-http-client")
    return builder
  }
}
