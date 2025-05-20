package pw.binom.strong.web.server

import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.properties.StrongProperties
import pw.binom.strong.properties.parse
import pw.binom.strong.web.server.controllers.LivenessController
import pw.binom.strong.web.server.controllers.ReadinessController
import pw.binom.strong.web.server.properties.WebServerProperties

object WebConfig {
  fun apply(config: StrongProperties) =
    Strong.config {
      val properties = config.parse<WebServerProperties>()
      it.bean { WebServerService() }
      if (properties.management != null) {
        it.bean { ManagementWebServerService() }
        it.bean { MetricsController() }
        it.bean { LivenessController() }
        it.bean { ReadinessController() }
      }
    }
}
