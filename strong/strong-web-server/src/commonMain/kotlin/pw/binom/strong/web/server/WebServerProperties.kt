package pw.binom.strong.web.server

import kotlinx.serialization.Serializable
import pw.binom.properties.serialization.annotations.PropertiesPrefix

@PropertiesPrefix("strong")
@Serializable
data class WebServerProperties(
  val server: ExternalWebServerProperties = ExternalWebServerProperties(port = 8080),
  val management: ExternalWebServerProperties? = ExternalWebServerProperties(port = 9090),
)
