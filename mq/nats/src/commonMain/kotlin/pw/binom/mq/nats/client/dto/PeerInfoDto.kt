package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.mq.nats.client.DurationNanoSerializer
import kotlin.time.Duration

@Serializable
data class PeerInfoDto(
  @SerialName("name")
  val name: String,
  @SerialName("current")
  val current: Boolean,
  @SerialName("offline")
  val offline: Boolean,
  @Serializable(DurationNanoSerializer::class)
  @SerialName("active")
  val active: Duration = Duration.ZERO,
  @SerialName("lag")
  val lag: Long,
)
