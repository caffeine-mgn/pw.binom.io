package pw.binom.strong.exceptions

import pw.binom.strong.getClassName
import kotlin.reflect.KClass

open class BeanCreateException(val clazz: KClass<out Any>, val name: String, cause: Throwable) :
    StrongException(cause) {
    override val message: String?
        get() =
            "Can't create bean $name (${clazz.getClassName()})"
}
