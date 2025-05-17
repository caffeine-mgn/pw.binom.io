package pw.binom.mq.nats

import pw.binom.mq.Topic
import pw.binom.mq.nats.client.NatsMessage

interface NatsTopic : Topic<NatsMessage, NatsProducer, NatsConsumer> {
  val subject: String

  override suspend fun createConsumer(group: String?, start: Boolean, func: suspend (NatsMessage) -> Unit): NatsConsumer
  override suspend fun createProducer(): NatsProducer
}
