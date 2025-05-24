package pw.binom.strong.nats.client

import kotlinx.serialization.Serializable

@Serializable
data class NatsProducerProperties(
  val topic: String,
)
