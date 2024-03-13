package pw.binom.strong.web.server

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.DEFAULT_BUFFER_SIZE
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
class ExternalWebServerProperties(
  val port: Int? = null,
  @SerialName("bind-addresses")
  val bindAddresses: List<String> = emptyList(),
  @SerialName("pool-size")
  val poolSize: Int = DEFAULT_BUFFER_SIZE,
  @SerialName("read-timeout")
  val readTimeout: Duration = Duration.INFINITE,
  @SerialName("max-request-length")
  val maxRequestLength: Int = 0,
  @SerialName("max-header-length")
  val maxHeaderLength: Int = 0,
  val compressing: Boolean = true,
  @SerialName("keep-alive")
  val keepAlive: Boolean = true,
  @SerialName("idle-check-tnterval")
  val idleCheckInterval: Duration = 5.seconds,
) {
  init {
    require(poolSize > 0) { "Invalid poolSize" }
  }
}
