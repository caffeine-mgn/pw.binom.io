package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.DateTimeRFC3339

@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_info_response")
data class ConsumerInfoResponseDto(
  @SerialName("stream_name")
  val streamName: String,
  val name: String,
  @Serializable(DateTimeRFC3339::class)
  val created: DateTime,
  val config: ConsumerConfiguration,
  val delivered: SequencePairDto,
  @SerialName("ack_floor")
  val ackFloor: SequencePairDto,
  val num_ack_pending: Long = 0,
  val num_redelivered: Long = 0,
  val num_waiting: Long = 0,
  val num_pending: Long = 0,
  val cluster: ClusterInfoDto? = null,
  val push_bound: Boolean = false,
  @Serializable(DateTimeRFC3339::class)
  val timestamp: DateTime? = null,
)
