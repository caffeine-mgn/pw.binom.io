package pw.binom.strong

import pw.binom.strong.exceptions.NoSuchBeanException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ServiceInjector<T : Any> internal constructor(strong: StrongImpl, beanClass: KClass<T>, name: String?) :
    AbstractServiceInjector<T, T>(
        strong = strong,
        beanClass = beanClass,
        name = name
    ) {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        init()
        if (bean == null) {
            throw NoSuchBeanException(beanClass, name)
        }
        return bean!!
    }
}
