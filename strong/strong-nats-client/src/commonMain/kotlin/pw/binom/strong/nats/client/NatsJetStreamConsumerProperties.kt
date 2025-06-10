package pw.binom.strong.nats.client

import kotlinx.serialization.Serializable
import pw.binom.mq.nats.client.AckPolicy
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.DeliverPolicy

@Serializable
data class NatsJetStreamConsumerProperties(
  val name: String,
  val durable: Boolean,
  val streamName: String,
  val autoCreate: Boolean = true,
  val memStorage: Boolean = false,
  val description: String? = null,
  val ackPolicy: AckPolicy = AckPolicy.NONE,
  val batchSize: Int = 100,
  val maxBytes: Int? = null,
  val deliverPolicy: DeliverPolicy? = null,
  val deliverGroup: String? = null,
  val flowControl: Boolean? = null,
) {
  val config
    get() = ConsumerConfiguration(
      durableName = if (durable) name else null,
      name = if (durable) null else name,
      memStorage = false,
      ackPolicy = AckPolicy.NONE,
      description = description,
      deliverPolicy = deliverPolicy,
      deliverGroup = deliverGroup,
      maxBytes = maxBytes,
      flowControl = flowControl,
    )
}
