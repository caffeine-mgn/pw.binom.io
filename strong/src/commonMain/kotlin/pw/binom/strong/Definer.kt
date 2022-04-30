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
        bean: (Strong) -> T
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
    noinline bean: (Strong) -> T
) {
    bean(
        clazz = T::class,
        name = name,
        ifNotExist = ifNotExist,
        bean = bean,
        primary = primary,
    )
}

inline fun <reified T : Closeable> Definer.beanClosable(
    name: String? = null,
    ifNotExist: Boolean = false,
    primary: Boolean = false,
    noinline bean: (Strong) -> T
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
                val bean = strong.injectOrNull<T>(name = name).service as Closeable?
                bean?.close()
            }
        }
    }
}
/*
inline fun <reified T : Any> Definer.findDefine(name: String? = null) =
    findDefine(T::class, name)
*/

@JvmName("beanAsyncCloseable")
inline fun <reified T : AsyncCloseable> Definer.beanAsyncCloseable(
    name: String? = null,
    ifNotExist: Boolean = false,
    noinline bean: (Strong) -> T
) {
    val defName = T::class.genDefaultName()
    bean(
        clazz = T::class,
        name = defName,
        ifNotExist = ifNotExist,
        bean = bean,
    )
    bean(name = "${T::class.genDefaultName()}_closable") {
        object : Strong.DestroyableBean {
            override suspend fun destroy(strong: Strong) {
                val bean = strong.injectOrNull<T>(name = defName).service as AsyncCloseable?
                bean?.asyncClose()
            }
        }
    }
}