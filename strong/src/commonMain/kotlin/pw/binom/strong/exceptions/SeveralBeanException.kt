package pw.binom.strong.exceptions

import kotlin.reflect.KClass

class SeveralBeanException(val klazz: KClass<out Any>, val name: String?) : StrongException() {
    override val message: String
        get() = if (name != null) {
            "Several bean $klazz with name $name"
        } else {
            "Several bean $klazz"
        }
}