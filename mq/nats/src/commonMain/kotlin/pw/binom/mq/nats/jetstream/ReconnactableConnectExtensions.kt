package pw.binom.mq.nats.jetstream

import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.JetStreamApi
import pw.binom.mq.nats.client.ReconnactableConnect
import pw.binom.mq.nats.client.dto.ErrorDto

suspend fun ReconnactableConnect.getOrCreateStreamConsumer(
  streamName: String,
  config: ConsumerConfiguration,
): JetStreamConsumer {
  if (config.durableName == null) {
    return createStreamConsumer(
      streamName = streamName,
      config = config
    )
  }
  val exist = JetStreamApi.getConsumerInfo(
    streamName = streamName,
    consumerName = config.durableName,
    client = this,
  )
  return when (exist.error?.code) {
    ErrorDto.NOT_FOUND -> createStreamConsumer(
      streamName = streamName,
      config = config
    )

    null -> JetStreamConsumer(
      streamName = streamName,
      consumerName = exist.name!!,
      connection = this,
      config = exist.config!!
    )

    else -> TODO("Unknown error code: ${exist.error}")
  }
}

suspend fun ReconnactableConnect.createStreamConsumer(
  streamName: String,
  config: ConsumerConfiguration,
): JetStreamConsumer {
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
  return createStreamConsumer(
    streamName = streamName,
    config = config,
  )
}
