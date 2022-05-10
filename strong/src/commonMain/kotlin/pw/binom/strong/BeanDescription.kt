package pw.binom.strong

import kotlin.reflect.KClass

interface BeanDescription {
    val bean: Any
    val name: String
    val beanClass: KClass<out Any>
}
