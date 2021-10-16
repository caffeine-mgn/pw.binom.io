package pw.binom.strong.exceptions

import pw.binom.strong.getClassName
import kotlin.reflect.KClass

class NoSuchBeanException(val klazz: KClass<out Any>, val name: String?) : StrongException() {
    override val message: String
        get() = "Bean ${klazz.getClassName()} not found"
}