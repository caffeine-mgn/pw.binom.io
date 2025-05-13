package pw.binom.strong

import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

interface Definer {
  /**
   * Define [bean]. Default [name] is `[bean]::class + "_" + [bean].class.hashCode()`
   *
   * @param bean object for define
   * @param name name of [bean] for define. See description of method for get default value
   * @param ifNotExist if false on duplicate will throw [BeanAlreadyDefinedException]. If true will ignore redefine
   */
//    suspend fun define(bean: Any, name: String? = null, ifNotExist: Boolean = false)
//    fun <T : Any> findDefine(clazz: KClass<T>, name: String? = null): T
  fun <T : Any> bean(
    clazz: KClass<T>,
    primary: Boolean = false,
    name: String? = null,
    ifNotExist: Boolean = false,
    bean: (Strong) -> T,
  )
}

fun <T : Any> KClass<T>.getClassName(): String {
  return this.toString()
    .removePrefix("class ")
    .removePrefix("interface ")
    .removeSuffix(" (Kotlin reflection is not available)")
}

fun <T : Any> KClass<T>.genDefaultName(): String =
  "${this.getClassName()}_${this.hashCode().toString(16)}"

inline fun <reified T : Any> Definer.bean(
  name: String? = null,
  primary: Boolean = false,
  ifNotExist: Boolean = false,
  noinline bean: (Strong) -> T,
) {
  bean(
    clazz = T::class,
    name = name,
    ifNotExist = ifNotExist,
    bean = bean,
    primary = primary,
  )
}

inline fun <reified T : Any> Definer.beanClosable(
  name: String? = null,
  ifNotExist: Boolean = false,
  primary: Boolean = false,
  noinline bean: (Strong) -> T,
  noinline closeProcessing: suspend (T) -> Unit,
) {
  bean(
    clazz = T::class,
    name = name,
    ifNotExist = ifNotExist,
    bean = bean,
    primary = primary,
  )
  bean(name = "${T::class.genDefaultName()}_closable") {
    object : Strong.DestroyableBean {
      override suspend fun destroy(strong: Strong) {
        val bean2 = strong.injectOrNull<T>(name = name).service as T?
        if (bean2 != null) {
          closeProcessing(bean2)
        }
      }
    }
  }
}

inline fun <reified T : Closeable> Definer.beanClosable(
  name: String? = null,
  ifNotExist: Boolean = false,
  primary: Boolean = false,
  noinline bean: (Strong) -> T,
) {
  beanClosable(
    name = name,
    ifNotExist = ifNotExist,
    primary = primary,
    bean = bean,
    closeProcessing = {
      it.close()
    }
  )
}

@JvmName("beanAsyncCloseable")
inline fun <reified T : AsyncCloseable> Definer.beanAsyncCloseable(
  name: String? = null,
  primary: Boolean = false,
  ifNotExist: Boolean = false,
  noinline bean: (Strong) -> T,
) {
  beanClosable(
    name = name,
    ifNotExist = ifNotExist,
    primary = primary,
    bean = bean,
    closeProcessing = {
      it.asyncClose()
    }
  )
}
