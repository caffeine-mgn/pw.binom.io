package pw.binom.mq.nats

import pw.binom.mq.Headers
import pw.binom.mq.nats.client.BytesParsedHeaders
import pw.binom.mq.nats.client.HeadersBody
import kotlin.coroutines.CoroutineContext

internal object DefaultEmptyCoroutineContext : CoroutineContext {
  override fun <R> fold(
    initial: R,
    operation: (R, CoroutineContext.Element) -> R,
  ): R = initial

  override fun plus(context: CoroutineContext): CoroutineContext = context

  override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? = null

  override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext = this
}

internal fun Headers.toNatsHeaders() =
  if (isEmpty) {
    HeadersBody.empty
  } else {
    val h = BytesParsedHeaders()
    forEach { (key, value) ->
      h.add(key, value)
    }
    h.toHeadersBody()
  }
