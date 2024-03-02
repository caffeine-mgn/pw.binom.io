package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.DateTimeRFC3339

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_update_response")
data class StreamUpdateResponseDto(
  val config: StreamConfig? = null,
  @Serializable(DateTimeRFC3339::class)
  val created: DateTime? = null,
  @SerialName("state")
  val state: StreamStateDto? = null,
  @SerialName("cluster")
  val cluster: ClusterInfoDto? = null,
  @Serializable(DateTimeRFC3339::class)
  @SerialName("ts")
  val ts: DateTime? = null,
)
