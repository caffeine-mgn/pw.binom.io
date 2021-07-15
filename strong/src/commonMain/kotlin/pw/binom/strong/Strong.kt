package pw.binom.strong

import pw.binom.strong.exceptions.StrongException
import kotlin.reflect.KClass

interface Strong {
    abstract class Bean {
        protected val strong: Strong =
            STRONG_LOCAL ?: throw IllegalStateException("Bean should be created during strong starts")
    }

    companion object {
        fun config(func: suspend (Definer) -> Unit) = object : Config {
            override suspend fun apply(strong: Definer) {
                func(strong)
            }
        }

//        fun serviceProvider(func: suspend (Definer) -> Unit) = object : ServiceProvider {
//            override suspend fun provide(definer: Definer) {
//                func(definer)
//            }
//        }

        suspend fun create(vararg config: Strong.Config): Strong {
            if (STRONG_LOCAL != null) {
                throw StrongException("Can't start strong inside strong")
            }
            val d = InternalDefiner()
            val strong = StrongImpl()
            config.forEach {
                it.apply(d)
            }
            Starter(strongImpl = strong, startDefiner = d).start()
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

    interface BeanFactory<T : Any> {
        val type: KClass<T>
        val name: String
            get() = type.genDefaultName()

        suspend fun provide(strong: Strong): T?
    }

//    interface ServiceProvider {
//        suspend fun provide(definer: Definer)
//    }

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