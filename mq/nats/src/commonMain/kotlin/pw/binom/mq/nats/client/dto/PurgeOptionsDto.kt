package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurgeOptionsDto(
  @SerialName("filter")
  val subject: String?,
  @SerialName("seq")
  val seq: Long = -1,
  @SerialName("keep")
  val keep: Long = -1,
)
