package pw.binom.logger

import pw.binom.collections.defaultMutableMap

actual fun createGlobalMap(): MutableMap<String, Logger> = defaultMutableMap()
