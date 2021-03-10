package pw.binom.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ServiceListInjector<T : Any>(val strong: StrongImpl, val beanClass: KClass<T>) {
    private val list by lazy {
        strong.beans.asSequence().filter {
            beanClass.isInstance(it.value)
        }.map { it.value as T }.toList()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> =
        list
}