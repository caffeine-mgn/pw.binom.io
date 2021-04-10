package pw.binom.strong

import pw.binom.strong.exceptions.SeveralBeanException
import kotlin.reflect.KClass

abstract class AbstractServiceInjector<T : Any, RESULT> internal constructor(
    internal var strong: StrongImpl,
    val beanClass: KClass<T>,
    val name: String?
) : ServiceProvider<RESULT> {
    protected var inited = false
    protected var bean: RESULT? = null

    protected fun init() {
        if (inited) {
            return
        }
        if (bean == null)
            bean = run {
                val it = strong.findBean(beanClass as KClass<Any>,name).iterator()
                if (!it.hasNext())
                    return@run null
                val bb = it.next()
                if (it.hasNext())
                    throw SeveralBeanException(beanClass, name)
                bb.value as RESULT
            }
        inited = true
    }

    override val service: RESULT
        get() = bean as RESULT
}