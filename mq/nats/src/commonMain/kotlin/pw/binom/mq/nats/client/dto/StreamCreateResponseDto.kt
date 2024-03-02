package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.DateTimeRFC3339

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_create_response")
data class StreamCreateResponseDto(
  val config: StreamConfig,
  @Serializable(DateTimeRFC3339::class)
  val created: DateTime,
  @SerialName("state")
  val state: StreamStateDto,
  @SerialName("cluster")
  val cluster: ClusterInfoDto? = null,
  @Serializable(DateTimeRFC3339::class)
  @SerialName("ts")
  val ts: DateTime,
  @SerialName("did_create")
  val didCreate: Boolean,
)
