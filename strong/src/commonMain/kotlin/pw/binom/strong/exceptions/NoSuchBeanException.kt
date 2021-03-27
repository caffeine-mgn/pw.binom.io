package pw.binom.strong.exceptions

import kotlin.reflect.KClass

class NoSuchBeanException(val klazz: KClass<out Any>) : StrongException() {
    override val message: String
        get() = "Bean $klazz not found"
}