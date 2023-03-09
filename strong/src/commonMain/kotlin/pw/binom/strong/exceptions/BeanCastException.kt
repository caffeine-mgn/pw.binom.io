package pw.binom.strong.exceptions

import pw.binom.strong.getClassName
import kotlin.reflect.KClass

class BeanCastException(val from: KClass<*>, val to: KClass<*>, val beanName: String) : StrongException() {
    override val message: String?
        get() = "Can't cast bean $beanName ${from.getClassName()} to ${to.getClassName()}"
}
