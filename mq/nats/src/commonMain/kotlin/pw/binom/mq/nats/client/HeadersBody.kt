package pw.binom.mq.nats.client

import kotlin.jvm.JvmInline

@JvmInline
value class HeadersBody(private val raw: ByteArray) {
  companion object {
    val empty = HeadersBody(NatsHeaders.emptyByteArray)
  }

  val isEmpty
    get() = raw.isEmpty()

  val isNotEmpty
    get() = !isEmpty

  val bytes
    get() = raw

  fun parse(): NatsHeaders {
    if (isEmpty) {
      return NatsHeaders.empty
    }
    val map = HashMap<String, ArrayList<String>>()
    raw.decodeToString(startIndex = BytesParsedHeaders.HEADER_VERSION.size)
      .lineSequence().forEach { line ->
        if (line.isEmpty()) {
          return@forEach
        }
        val items = line.split(':', limit = 2)
        map.getOrPut(items[0]) { ArrayList() }.add(items[1].removePrefix(" "))
      }
    return ParsedHeadersMap(map)
  }

  fun clone() = if (isEmpty) empty else HeadersBody(raw)

  operator fun plus(other: HeadersBody) = HeadersBody(bytes + other.bytes)

  operator fun plus(other: NatsHeaders) = this + other.toHeadersBody()
}
