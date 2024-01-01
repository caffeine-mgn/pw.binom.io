package pw.binom.strong

import pw.binom.strong.exceptions.SeveralBeanException
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class AbstractServiceInjector<T : Any, RESULT> internal constructor(
  internal var strong: StrongImpl,
  val beanClass: KClass<T>,
  val name: String?,
) : ServiceProvider<RESULT> {
  protected var inited = false
  protected var bean: RESULT? = null

  override fun isInitialized(): Boolean = inited

  protected fun init() {
    if (inited) {
      return
    }
    if (bean == null) {
      bean = run {
        val st = strong.findBean(beanClass as KClass<Any>, name).toList()
        if (name == null && st.size > 1) {
          val primaryBean = st.filter { it.value.primary }
          if (primaryBean.size > 1) {
            throw SeveralBeanException(beanClass, name)
          }
          if (primaryBean.size == 1) {
            return@run primaryBean[0].value.bean as RESULT
          }
        }
        if (st.isEmpty()) {
          println("Beans:\n${strong.beans}\n${strong.beans.entries.joinToString()}")
          return@run null
        }
        if (st.size == 1) {
          return@run st[0].value.bean as RESULT
        }
        throw SeveralBeanException(beanClass, name)
      }
    }
    inited = true
  }

  override val service: RESULT
    get() {
      init()
      return bean as RESULT
    }

  override val value: RESULT
    get() = service
}
