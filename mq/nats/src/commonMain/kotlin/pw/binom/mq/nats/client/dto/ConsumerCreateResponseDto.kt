package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.DateTimeRFC3339

@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_create_response")
class ConsumerCreateResponseDto(
  @SerialName("stream_name")
  val streamName: String,
  @SerialName("name")
  val name: String,
  @Serializable(DateTimeRFC3339::class)
  @SerialName("created")
  val created: DateTime,
  @SerialName("config")
  val config: ConsumerConfiguration,
  @SerialName("delivered")
  val delivered: SequencePairDto,
  @SerialName("ack_floor")
  val ackFloor: SequencePairDto,
  @SerialName("num_ack_pending")
  val numAckPending: Long = 0,
  @SerialName("num_redelivered")
  val numRedelivered: Long = 0,
  @SerialName("num_waiting")
  val numWaiting: Long = 0,
  @SerialName("num_pending")
  val numPending: Long = 0,
  @SerialName("cluster")
  val cluster: ClusterInfoDto? = null,
  @SerialName("push_bound")
  val pushBound: Boolean = false,
  @Serializable(DateTimeRFC3339::class)
  @SerialName("timestamp")
  val timestamp: DateTime? = null,
)
