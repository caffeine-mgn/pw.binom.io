package pw.binom.mq.nats.client

import pw.binom.mq.MapHeaders

class ParsedHeadersMap(
  map: Map<String, List<String>>,
  override val code: Int,
  override val message: String,
) : NatsHeaders, MapHeaders(map) {
  override fun get(key: String): List<String>? = map[key]

  override fun clone() = ParsedHeadersMap(HashMap(map), code = code, message = message)

  override fun toHeadersBody() = BytesParsedHeaders(map).toHeadersBody()
}
