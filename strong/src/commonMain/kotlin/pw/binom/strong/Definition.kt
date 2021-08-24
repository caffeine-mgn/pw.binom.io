package pw.binom.strong

import kotlin.reflect.KClass

internal class Definition(
    val name: String,
    val primary:Boolean,
    val clazz: KClass<out Any>,
    val init: (Strong) -> Any,
)