package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReplayPolicy {
  @SerialName("instant")
  INSTANT,

  @SerialName("original")
  ORIGINAL,
}
