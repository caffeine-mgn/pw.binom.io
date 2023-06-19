package pw.binom.strong

inline fun <reified T : Any> inject(name: String? = null) = getStrong().inject<T>(name = name)
inline fun <reified T : Any> injectServiceMap() = getStrong().injectServiceMap<T>()
inline fun <reified T : Any> injectServiceList() = getStrong().injectServiceList<T>()
inline fun <reified T : Any> injectOrNull(name: String? = null) = getStrong().injectOrNull<T>(name = name)
