package pw.binom.logger

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LoggerDelegator(val logger: Logger) : ReadOnlyProperty<Any, Logger> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Logger = logger
}
