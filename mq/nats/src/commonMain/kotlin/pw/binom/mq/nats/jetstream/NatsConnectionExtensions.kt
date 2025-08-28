package pw.binom.mq.nats.jetstream

import pw.binom.mq.nats.client.JetStreamApi
import pw.binom.mq.nats.client.NatsConnection
import pw.binom.mq.nats.client.ReconnactableConnect
import pw.binom.mq.nats.client.dto.ErrorDto


suspend fun ReconnactableConnect.getStreamConsumer(
  streamName: String,
  consumerName: String,
): JetStreamConsumer? {
  val info = JetStreamApi.getConsumerInfo(streamName = streamName, consumerName = consumerName, client = this)
  if (info.error?.code == ErrorDto.NOT_FOUND) {
    return null
  }
  return JetStreamConsumer(
    streamName = streamName,
    consumerName = consumerName,
    connection = this,
    config = info.config!!
  )
}


suspend fun NatsConnection.getStream(
  name: String,
  offset: Int = 0,
  deletedDetails: Boolean = false,
  subjectsFilter: String? = null,
) = JetStreamApi.getStreamInfo(
  client = this,
  name = name,
  offset = offset,
  deletedDetails = deletedDetails,
  subjectsFilter = subjectsFilter,
).let {
  if (it.error?.code == ErrorDto.NOT_FOUND) {
    null
  } else {
    it
  }
}
