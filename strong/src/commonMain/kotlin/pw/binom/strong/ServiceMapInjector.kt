package pw.binom.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ServiceMapInjector<T : Any>(val strong: StrongImpl, val beanClass: KClass<T>) {
    private val map by lazy {
        strong.beans.asSequence().filter {
            beanClass.isInstance(it.value)
        }
            .map { it.key to it.value as T }
            .toMap()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Map<String, T> =
        map
}