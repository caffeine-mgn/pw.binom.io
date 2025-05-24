package pw.binom.strong.nats.client

import kotlinx.serialization.Serializable
import pw.binom.mq.nats.client.DeliverPolicy

@Serializable
data class NatsJetStreamConsumerProperties(
  val name: String,
  val durable: Boolean,
  val topic: String,
  val autoCreate: Boolean = true,
  val memStorage: Boolean = false,
  val description: String? = null,
  val batchSize: Int = 100,
  val deliverPolicy: DeliverPolicy? = null,
)
