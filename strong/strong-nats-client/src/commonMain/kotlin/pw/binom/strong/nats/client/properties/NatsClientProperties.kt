package pw.binom.strong.nats.client.properties

import kotlinx.serialization.Serializable
import pw.binom.properties.serialization.annotations.PropertiesPrefix
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@PropertiesPrefix("strong.nats.client")
@Serializable
data class NatsClientProperties(
  val enabled: Boolean = true,
  val host: String,
  val port: Int = 4222,
  val lazyStart: Boolean = true,
  val lang: String = "kotlin",
  val clientName: String? = null,
  val clientVersion: String = "1.0.x",
  val allowEcho: Boolean = true,
  val reconnectDelay: Duration = 10.seconds,
  val auth: Auth? = null,
) {
  @Serializable
  data class Auth(val user: String, val password: String)
}
