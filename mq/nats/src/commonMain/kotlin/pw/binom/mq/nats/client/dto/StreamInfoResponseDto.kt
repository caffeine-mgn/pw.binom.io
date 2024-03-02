package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.DateTimeRFC3339

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_info_response")
data class StreamInfoResponseDto(
  val total: Int?,
  val offset: Int?,
  val limit: Int?,
  val error: ErrorDto? = null,
  @SerialName("created")
  @Serializable(DateTimeRFC3339::class)
  val createTime: DateTime? = null,
  @SerialName("config")
  val config: StreamConfig? = null,
  @SerialName("state")
  val state: StreamStateDto? = null,
  @SerialName("cluster")
  val cluster: ClusterInfoDto? = null,
  // TODO не тот тип
  @SerialName("mirror")
  val mirror: SourceBase? = null,
  // TODO не тот тип
  @SerialName("sources")
  val sources: List<SourceBase>? = null,
  @SerialName("timestamp")
  @Serializable(DateTimeRFC3339::class)
  val timestamp: DateTime? = null,
)
