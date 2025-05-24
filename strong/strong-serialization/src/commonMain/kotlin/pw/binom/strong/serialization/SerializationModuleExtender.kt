package pw.binom.strong.serialization

import kotlinx.serialization.modules.SerializersModule

interface SerializationModuleExtender {
  fun extends(module: SerializersModule): SerializersModule
}
