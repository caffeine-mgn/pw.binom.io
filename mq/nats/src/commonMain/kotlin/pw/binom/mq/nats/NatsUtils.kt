package pw.binom.mq.nats

import pw.binom.mq.Headers
import pw.binom.mq.nats.client.BytesParsedHeaders
import pw.binom.mq.nats.client.HeadersBody
import pw.binom.mq.nats.client.NatsHeaders
import pw.binom.mq.nats.client.ParsedHeadersMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal val SPILLER: ByteArray = byteArrayOf(0x3a, 0x20)

@PublishedApi
internal val LINE_END = byteArrayOf(0x0d, 0x0a)

@PublishedApi
internal val NORMAL_HEADER_LENGTH = 8

@OptIn(ExperimentalContracts::class, ExperimentalStdlibApi::class)
internal inline fun parseFirstHeaderLine(data: ByteArray, func: (Int, String) -> Unit): Int {
  contract {
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  val l = data.find(LINE_END)
  if (l == -1 || l < NORMAL_HEADER_LENGTH) {
    println("l=$l   ${data.decodeToString()}")
    func(0, "")
    throw RuntimeException("Invalid protocol")
  }
  if (l == NORMAL_HEADER_LENGTH) {
    func(0, "")
    return l + 2
  }
  val space = data.find(0x20, startIndex = 9, endIndex = l)
  val code = data.decodeToString(9, space).toInt()
  val msg = data.decodeToString(space + 1, l)
  func(code, msg)
  return l + 2
}

internal fun parseHeaders(data: ByteArray): NatsHeaders {
  val code: Int
  val msg: String
  var s = parseFirstHeaderLine(data) { c, m ->
    code = c
    msg = m
  }
  val result = HashMap<String, ArrayList<String>>()
  while (true) {
    val end = data.find(LINE_END, startIndex = s)
    if (end == -1) {
      break
    }
    if (end - s == 0) {
      break
    }
    val spillter = data.find(SPILLER, startIndex = s, endIndex = end)
    val key = data.copyOfRange(fromIndex = s, toIndex = spillter).decodeToString()
    val value = data.copyOfRange(fromIndex = spillter + 2, toIndex = end).decodeToString()
    result.getOrPut(key) { ArrayList() }.add(value)
    s = end + 2
  }
  return ParsedHeadersMap(map = result, code = code, message = msg)
}

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
