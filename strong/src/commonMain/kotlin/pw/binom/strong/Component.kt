package pw.binom.strong

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Component(
    val config: KClass<out DynamicConfig>,
    val name: String = "",
    val primary: Boolean = false,
    val ifNotExist: Boolean = false,
)
