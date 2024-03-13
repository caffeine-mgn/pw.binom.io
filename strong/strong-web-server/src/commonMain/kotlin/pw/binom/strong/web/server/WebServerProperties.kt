package pw.binom.strong.web.server

import kotlinx.serialization.Serializable
import pw.binom.properties.serialization.annotations.PropertiesPrefix

@PropertiesPrefix("strong")
@Serializable
data class WebServerProperties(
  val server: ExternalWebServerProperties? = null,
  val management: ExternalWebServerProperties? = null,
)
