package pw.binom.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal class ServiceMapInjector<T : Any>(val strong: StrongImpl, val beanClass: KClass<T>) :
    ServiceProvider<Map<String,T>>{
    private val map by lazy {
        strong.findBean(beanClass as KClass<Any>,null).map { it.key to it.value.bean as T }
            .toMap()
    }
    override val service: Map<String, T>
        get() = map

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Map<String, T> =
        service
}