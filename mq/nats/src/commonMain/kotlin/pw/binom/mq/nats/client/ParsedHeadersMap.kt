package pw.binom.mq.nats.client

import pw.binom.mq.MapHeaders

class ParsedHeadersMap(map: Map<String, List<String>>) : NatsHeaders, MapHeaders(map) {
  override fun get(key: String): List<String>? = map[key]

  override fun clone() = ParsedHeadersMap(HashMap(map))

  override fun toHeadersBody() = BytesParsedHeaders(map).toHeadersBody()
}
