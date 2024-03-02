package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
  val code: Int,
  @SerialName("err_code")
  val errCode: Int,
  val description: String,
)
