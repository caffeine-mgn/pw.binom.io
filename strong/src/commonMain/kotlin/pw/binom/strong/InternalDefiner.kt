package pw.binom.strong

import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import kotlin.reflect.KClass

internal class InternalDefiner : Definer {
    private val alreadyDefined = HashMap<String, Throwable>()
    private val definitions = ArrayList<Definition>()
    fun getLastDefinitions(): List<Definition> {
        val l = ArrayList(definitions)
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
            name = clazz.genDefaultName(),
            clazz = clazz,
            init = bean,
            primary = primary,
        )
    }
}