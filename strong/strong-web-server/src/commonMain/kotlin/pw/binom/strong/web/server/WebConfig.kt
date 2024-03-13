package pw.binom.strong.web.server

import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.properties.StrongProperties

object WebConfig {
  fun apply(config: StrongProperties) =
    Strong.config {
      val properties = config.parse<WebServerProperties>()
      if (properties.server == null) {
        it.bean { WebServerService() }
      }
      if (properties.management == null) {
        it.bean { ManagementWebServerService() }
        it.bean { HealthController() }
        it.bean { MetricsController() }
      }
    }
}
