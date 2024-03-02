package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClusterInfoDto(
  @SerialName("name")
  val name: String? = null,
  @SerialName("leader")
  val leader: String,
  @SerialName("replicas")
  val replicas: List<PeerInfoDto>? = null,
)
