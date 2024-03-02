package pw.binom.mq.nats.client

import pw.binom.mq.Headers

interface NatsHeaders : Headers {
  companion object {
    const val NATS_SUBJECT = "Nats-Subject"
    const val NATS_SEQUENCE = "Nats-Sequence"
    const val NATS_TIMESTAMP = "Nats-Time-Stamp"
    const val NATS_STREAM = "Nats-Stream"
    const val NATS_LAST_SEQUENCE = "Nats-Last-Sequence"
    internal val emptyByteArray = byteArrayOf()
    val empty: NatsHeaders =
      object : NatsHeaders {
        override fun toHeadersBody() = HeadersBody.empty

        override fun get(key: String): List<String>? = Headers.empty[key]

        override fun clone() = this

        override val size: Int
          get() = Headers.empty.size

        override fun iterator(): Iterator<Pair<String, String>> = Headers.empty.iterator()
      }

    fun build(func: MutableParsedHeaders.() -> Unit): MutableParsedHeaders {
      val out = BytesParsedHeaders()
      func(out)
      return out
    }
  }

  override fun clone(): NatsHeaders

  fun toHeadersBody(): HeadersBody

  override fun filter(func: (key: String, value: String) -> Boolean): NatsHeaders {
    val map = HashMap<String, ArrayList<String>>()
    forEach { (key, value) ->
      if (func(key, value)) {
        map.getOrPut(key) { ArrayList() }.add(value)
      }
    }
    return if (map.isEmpty()) {
      empty
    } else {
      map.forEach {
        it.value.trimToSize()
      }
      BytesParsedHeaders(map)
    }
  }
}
