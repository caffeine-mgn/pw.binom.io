package pw.binom.strong.exceptions

import pw.binom.strong.getClassName
import kotlin.reflect.KClass

class SeveralBeanException(val klazz: KClass<out Any>, val name: String?) : StrongException() {
    override val message: String
        get() = if (name != null) {
            "Several bean with class ${klazz.getClassName()} and with name $name"
        } else {
            "Several bean with class ${klazz.getClassName()}"
        }
}
