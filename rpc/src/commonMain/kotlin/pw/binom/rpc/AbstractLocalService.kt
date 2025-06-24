package pw.binom.rpc

import pw.binom.collections.defaultMutableMap
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

abstract class AbstractLocalService : CrossService {
    private val methods = defaultMutableMap<String, CrossService.CrossMethod<Any?,Any?>>()
    fun findMethod(name: String) = methods[name]

    class LocalMethod<REQUEST,RESPONSE> internal constructor(val name: String, val func: suspend (REQUEST) -> RESPONSE) :
        CrossService.CrossMethod<REQUEST,RESPONSE> {
        override suspend operator fun invoke(params: REQUEST)= func(params)
        override fun getValue(thisRef: Any, property: KProperty<*>): CrossService.CrossMethod<REQUEST,RESPONSE> = this
    }

  @Suppress("UNCHECKED_CAST")
    inner class Provider<REQUEST,RESPONSE>(val func: suspend (REQUEST) -> RESPONSE) :
        PropertyDelegateProvider<Any, LocalMethod<REQUEST,RESPONSE>> {
        override fun provideDelegate(thisRef: Any, property: KProperty<*>): LocalMethod<REQUEST,RESPONSE> {
            val m = LocalMethod(property.name, func)
            methods[property.name] = m as CrossService.CrossMethod<Any?,Any?>
            return m
        }
    }

    protected fun <REQUEST,RESPONSE> local(func: suspend (REQUEST) -> RESPONSE) = Provider(func)
}
