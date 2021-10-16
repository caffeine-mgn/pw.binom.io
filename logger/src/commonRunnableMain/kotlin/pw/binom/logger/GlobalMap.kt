package pw.binom.logger

import pw.binom.collection.FrozenHashMap

actual fun createGlobalMap():MutableMap<String, Logger> = FrozenHashMap()