package pw.binom.strong.nats.client

import kotlinx.serialization.Serializable

@Serializable
data class NatsConsumerProperties(
  val topic: String,
  val group: String? = null,
)
