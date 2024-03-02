package pw.binom.mq.nats.client

interface MutableParsedHeaders : NatsHeaders {
  fun add(
    key: String,
    value: String,
  )

  operator fun Pair<String, String>.unaryPlus() = add(this)

  fun add(value: Pair<String, String>) = add(key = value.first, value = value.second)

  operator fun plusAssign(value: Pair<String, String>) = add(key = value.first, value = value.second)
}
