package pw.binom.rpc

import pw.binom.collections.defaultMutableMap
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

abstract class AbstractLocalService : CrossService {
    private val methods = defaultMutableMap<String, CrossService.CrossMethod<Any?>>()
    fun findMethod(name: String) = methods[name]

    class LocalMethod<T> internal constructor(val name: String, val func: suspend (Map<String, Any?>) -> T) :
        CrossService.CrossMethod<T> {
        override suspend operator fun invoke(params: Map<String, Any?>): T = func(params)
        override fun getValue(thisRef: Any, property: KProperty<*>): CrossService.CrossMethod<T> = this
    }

  @Suppress("UNCHECKED_CAST")
    inner class Provider<T>(val func: suspend (Map<String, Any?>) -> T) :
        PropertyDelegateProvider<Any, LocalMethod<T>> {
        override fun provideDelegate(thisRef: Any, property: KProperty<*>): LocalMethod<T> {
            val m = LocalMethod(property.name, func)
            methods[property.name] = m as CrossService.CrossMethod<Any?>
            return m
        }
    }

    protected fun <T> local(func: suspend (Map<String, Any?>) -> T) = Provider(func)
}
