package pw.binom.strong

import kotlin.reflect.KClass

internal class DefinitionImpl(
    val name: String,
    val primary: Boolean,
    val clazz: KClass<out Any>,
    val init: (Strong) -> Any,
)
