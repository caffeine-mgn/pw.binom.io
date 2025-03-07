package pw.binom.http.client

import kotlinx.coroutines.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.LinkedList
import pw.binom.collections.removeIf
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.DateTime
import pw.binom.io.AsyncCloseable
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.http.mutableHeadersOf
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
) : AsyncCloseable {
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


    suspend fun connect(method: String, url: URL, headers: Headers): HttpClientExchange {
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

    private fun <K, V> MutableMap<K, LinkedList<V>>.removeLastOrNull(key: K): V? {
        val list = this[key] ?: return null
        val value = list.removeLast()
        if (list.isEmpty()) {
            remove(key)
        }
        return value
    }

    private fun <K, V> MutableMap<K, LinkedList<V>>.addFirst(key: K, value: V) {
        var list = this[key]
        if (list == null) {
            list = LinkedList()
            this[key] = list
        }
        list.addFirst(value)
    }

    private fun <K, V> MutableMap<K, LinkedList<V>>.removeFromListIf(func: (K, V) -> Boolean) {
        val it = entries.iterator()
        while (it.hasNext()) {
            val valueList = it.next()
            valueList.value.removeIf { value ->
                func(valueList.key, value)
            }
            if (valueList.value.isEmpty()) {
                it.remove()
            }
        }
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

    fun request(method: String, url: URL, headers: Headers = emptyHeaders()): HttpRequestBuilder {
        val sendingHeaders = mutableHeadersOf(
            Headers.CONNECTION to Headers.KEEP_ALIVE,
            Headers.HOST to url.domain,
            Headers.SERVER to "binom-http-client",
        )
        sendingHeaders.addAll(headers)
        return HttpRequestBuilder(
            url = url,
            method = method,
            client = this,
            headers = sendingHeaders
        )
    }
}
