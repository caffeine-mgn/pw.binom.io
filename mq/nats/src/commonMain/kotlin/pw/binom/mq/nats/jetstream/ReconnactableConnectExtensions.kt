package pw.binom.mq.nats.jetstream

import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.JetStreamApi
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

suspend fun ReconnactableConnect.getOrCreateConsumer(
  streamName: String,
  config: ConsumerConfiguration,
): JetStreamConsumer {
  val exist = getStreamConsumer(
    streamName = streamName,
    consumerName = config.consumerName,
  )
  if (exist != null) {
    return exist
  }
  return createConsumer(
    streamName = streamName,
    config = config,
  )
}
