package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDeleteRequestDto(
  @SerialName("seq")
  val sequence: Long,
  @SerialName("no_erase")
  val noErase: Boolean = true,
)
