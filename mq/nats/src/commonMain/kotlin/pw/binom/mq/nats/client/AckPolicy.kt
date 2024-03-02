package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AckPolicy {
  @SerialName("none")
  NONE,

  @SerialName("all")
  ALL,

  @SerialName("explicit")
  EXPLICIT,
}
