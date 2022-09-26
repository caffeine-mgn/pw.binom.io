package pw.binom.strong

import pw.binom.collections.defaultArrayList
import pw.binom.collections.defaultHashMap
import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import kotlin.reflect.KClass

internal class InternalDefiner : Definer {
    private val alreadyDefined = defaultHashMap<String, Throwable>()
    private val definitions = defaultArrayList<Definition>()
    fun getLastDefinitions(): List<Definition> {
        val l = defaultArrayList(definitions)
        definitions.clear()
        return l
    }

    override fun <T : Any> bean(
        clazz: KClass<T>,
        primary: Boolean,
        name: String?,
        ifNotExist: Boolean,
        bean: (Strong) -> T
    ) {
        val defName = clazz.genDefaultName()
        val defEx = alreadyDefined[name]
        if (defEx != null) {
            throw BeanAlreadyDefinedException(beanName = defName, cause = defEx)
        }
        alreadyDefined[defName] = BeanAlreadyDefinedException(beanName = defName)
        definitions += Definition(
            name = name ?: clazz.genDefaultName(),
            clazz = clazz,
            init = bean,
            primary = primary,
        )
    }
}
