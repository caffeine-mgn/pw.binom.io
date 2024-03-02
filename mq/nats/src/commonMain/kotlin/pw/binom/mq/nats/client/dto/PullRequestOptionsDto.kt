package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.mq.nats.client.DurationNanoSerializer
import kotlin.time.Duration

@Serializable
data class PullRequestOptionsDto(
  @SerialName("batch")
  val batch: Int = -1,
  @SerialName("max_bytes")
  val maxBytes: Long = -1,
  @SerialName("no_wait")
  val noWait: Boolean = false,
  @SerialName("expires")
  @Serializable(DurationNanoSerializer::class)
  val expires: Duration? = null,
  @SerialName("idle_heartbeat")
  @Serializable(DurationNanoSerializer::class)
  val idleHeartbeat: Duration? = null,
)
