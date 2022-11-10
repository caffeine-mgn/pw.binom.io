package pw.binom.logger

import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

class LoggerPropertyDelegatorProvider : PropertyDelegateProvider<Any, LoggerDelegator> {
    override fun provideDelegate(thisRef: Any, property: KProperty<*>): LoggerDelegator {
        val name = thisRef::class.simpleName ?: return LoggerDelegator(Logger.global)
        return LoggerDelegator(Logger.getLogger(name))
    }
}
