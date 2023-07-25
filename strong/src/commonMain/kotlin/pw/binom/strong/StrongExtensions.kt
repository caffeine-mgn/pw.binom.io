package pw.binom.strong

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

inline fun <reified T : Any> inject(name: String? = null) = getStrong().inject<T>(name = name)
inline fun <reified T : Any> injectServiceMap() = getStrong().injectServiceMap<T>()
inline fun <reified T : Any> injectServiceList() = getStrong().injectServiceList<T>()
inline fun <reified T : Any> injectOrNull(name: String? = null) = getStrong().injectOrNull<T>(name = name)

@JvmName("inject2")
inline fun <reified T : Any> KClass<T>.inject(name: String? = null) = getStrong().inject<T>(name = name)
@JvmName("injectServiceMap2")
inline fun <reified T : Any> KClass<T>.injectServiceMap() = getStrong().injectServiceMap<T>()
@JvmName("injectServiceList2")
inline fun <reified T : Any> KClass<T>.injectServiceList() = getStrong().injectServiceList<T>()
@JvmName("injectOrNull2")
inline fun <reified T : Any> KClass<T>.injectOrNull(name: String? = null) = getStrong().injectOrNull<T>(name = name)
