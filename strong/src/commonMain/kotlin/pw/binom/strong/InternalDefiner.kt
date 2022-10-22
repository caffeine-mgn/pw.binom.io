package pw.binom.strong

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableList2
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import kotlin.reflect.KClass

internal class InternalDefiner : Definer {
    private val alreadyDefined = defaultMutableMap<String, Throwable>().useName("InternalDefiner.alreadyDefined")
    private val definitions = defaultMutableList2<Definition>()
    fun getLastDefinitions(): List<Definition> {
        val l = defaultMutableList(definitions)
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
