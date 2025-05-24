package pw.binom.strong.serialization

import pw.binom.strong.Strong
import pw.binom.strong.bean

object SerializationConfig {
  fun base() = Strong.config {
    it.bean { SerializationService() }
  }
}
