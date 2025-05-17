package pw.binom.strong.nats.client

import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.nats.client.properties.NatsClientProperties
import pw.binom.strong.properties.StrongProperties

object NatsClientConfig {
  fun apply(config: StrongProperties) =
    Strong.config {
      val properties = config.parse<NatsClientProperties>()
      if (properties.enabled) {
        it.bean { NatsServiceProvider() }
      }
    }
}
