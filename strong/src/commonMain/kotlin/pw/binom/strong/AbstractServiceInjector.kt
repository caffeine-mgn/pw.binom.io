package pw.binom.strong

import pw.binom.strong.exceptions.SeveralBeanException
import kotlin.reflect.KClass

abstract class AbstractServiceInjector<T : Any> internal constructor(
    val strong: StrongImpl,
    val beanClass: KClass<T>,
    val name: String?
) {
    private var inited = false
    private var bean: T? = null

    protected val value: T?
        get() {
            if (inited) {
                return bean
            }
            if (bean == null)
                bean = run {
                    val vv = strong.beans.asSequence().filter {
                        beanClass.isInstance(it.value) && (name == null || it.key == name)
                    }
                    val it = vv.iterator()
                    if (!it.hasNext())
                        return@run null
                    val bb = it.next()
                    if (it.hasNext())
                        throw SeveralBeanException(beanClass, name)
                    bb.value as T
                }
            inited = true
            return bean
        }
}