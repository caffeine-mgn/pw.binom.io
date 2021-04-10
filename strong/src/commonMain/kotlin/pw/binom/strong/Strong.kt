package pw.binom.strong

import kotlin.reflect.KClass

interface Strong {

    companion object {
        fun config(func: suspend (Definer) -> Unit) = object : Config {
            override suspend fun apply(strong: Definer) {
                func(strong)
            }
        }

        fun serviceProvider(func: suspend (Definer) -> Unit) = object : ServiceProvider {
            override suspend fun provide(strong: Definer) {
                func(strong)
            }
        }

        suspend fun create(vararg config: Strong.Config): Strong {
            val d = DefinerImpl()
            val strong = StrongImpl()
            config.forEach {
                it.apply(d)
            }
            strong.start(d.createBeans())
            return strong
        }
    }

    fun <T : Any> service(beanClass: KClass<T>, name: String? = null): pw.binom.strong.ServiceProvider<T>
    fun <T : Any> serviceMap(beanClass: KClass<T>): pw.binom.strong.ServiceProvider<Map<String, T>>
    fun <T : Any> serviceList(beanClass: KClass<T>): pw.binom.strong.ServiceProvider<List<T>>
    fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String? = null): pw.binom.strong.ServiceProvider<T?>
    suspend fun destroy()

    /**
     * Returns true if bean with [name] already defined
     *
     * @return true if bean with [name] already defined
     */
    operator fun contains(beanName: String): Boolean

    /**
     * Returns true if bean with class [klass] with default name already defined
     *
     * @return true if bean of class [klass] defined with default name
     */
    operator fun <T : Any> contains(clazz: KClass<T>): Boolean

    interface Config {
        suspend fun apply(strong: Definer)
    }

    interface InitializingBean {
        suspend fun init(strong: Strong)
    }

    interface LinkingBean {
        suspend fun link(strong: Strong)
    }

    interface ServiceProvider {
        suspend fun provide(strong: Definer)
    }

    interface DestroyableBean {
        suspend fun destroy(strong: Strong)
    }
}

inline fun <reified T : Any> Strong.inject(name: String? = null) = service(T::class, name)
inline fun <reified T : Any> Strong.injectServiceMap() = serviceMap(T::class)
inline fun <reified T : Any> Strong.injectServiceList() = serviceList(T::class)
inline fun <reified T : Any> Strong.injectOrNull(name: String? = null) = serviceOrNull(T::class, name)

/**
 * Returns true if bean with class [T] with default name already defined
 *
 * @return true if bean of class [T] defined with default name
 */
inline fun <reified T : Any> Strong.exist() = contains(T::class)