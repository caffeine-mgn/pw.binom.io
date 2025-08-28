package pw.binom.mq.nats.jetstream

import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.JetStreamApi
import pw.binom.mq.nats.client.NatsConnection
import pw.binom.mq.nats.client.ReconnactableConnect

suspend fun ReconnactableConnect.createConsumer(streamName: String, config: ConsumerConfiguration): JetStreamConsumer {
  val info = JetStreamApi.createConsumer(
    client = this,
    streamName = streamName,
    config = config
  )
  return JetStreamConsumer(
    streamName = info.streamName,
    consumerName = info.name,
    connection = this,
    config = info.config
  )
}
