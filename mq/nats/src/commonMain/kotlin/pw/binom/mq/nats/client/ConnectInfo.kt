package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.mq.nats.serialization.DomainSocketAddressSerializer

@Serializable
data class ConnectInfo(
  @SerialName("server_id")
  val serverId: String,
  @SerialName("server_name")
  val serverName: String,
  @SerialName("max_payload")
  val maxPayload: Long,
  @SerialName("client_id")
  val clientId: Int,
  @SerialName("proto")
  val proto: Int,
  @SerialName("jetstream")
  val jetStreamEnabled: Boolean = false,
  @SerialName("auth_required")
  val authRequired: Boolean = false,
  @SerialName("headers")
  val headersSupported: Boolean = false,
  @SerialName("connect_urls")
  val clusterAddresses: List<@Serializable(DomainSocketAddressSerializer::class) DomainSocketAddress> = emptyList(),
)
