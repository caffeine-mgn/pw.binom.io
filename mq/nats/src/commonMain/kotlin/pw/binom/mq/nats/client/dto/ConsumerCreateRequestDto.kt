package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.mq.nats.client.ConsumerConfiguration

@Serializable
class ConsumerCreateRequestDto(
  @SerialName("stream_name")
  val streamName: String,
  @SerialName("config")
  val config: ConsumerConfiguration,
)
