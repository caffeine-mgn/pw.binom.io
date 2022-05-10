package pw.binom.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal class ServiceListInjector<T : Any>(
    val strong: StrongImpl,
    val beanClass: KClass<T>
) : ServiceProvider<List<T>> {
    private val list by lazy {
        strong.findBean(beanClass as KClass<Any>, null).map { it.value.bean as T }.toList()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> =
        list

    override val service: List<T>
        get() = list
}
