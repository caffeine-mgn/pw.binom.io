package pw.binom.strong

import pw.binom.collections.defaultMutableList2
import pw.binom.strong.exceptions.StrongException
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private class LazyInitPropertyDelegateProvider<T>(val init: suspend () -> T) :
    PropertyDelegateProvider<Strong.Bean, AfterInit<T>> {
    lateinit var delegator: AfterInit<T>
    override fun provideDelegate(thisRef: Strong.Bean, property: KProperty<*>): AfterInit<T> {
        delegator = AfterInit(fieldName = property.name, init = init)
        return delegator
    }
}

private class AfterInit<T>(val fieldName: String, val init: suspend () -> T) : ReadOnlyProperty<Strong.Bean, T> {
    private var inited = false
    private var value: T? = null

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Strong.Bean, property: KProperty<*>): T {
        if (!inited) {
            throw StrongException("Value not inited yet. Make sure Strong.Bean.init or Strong.Bean.link called")
        }
        return value as T
    }

    suspend fun initValue() {
        if (inited) {
            return
        }
        inited = true
        value = init()
    }
}

interface Strong {
    abstract class Bean : InitializingBean, LinkingBean {
        protected val strong: Strong =
            STRONG_LOCAL ?: throw IllegalStateException("Bean should be created during strong starts")

        protected inline fun <reified T : Any> inject(name: String? = null) = strong.inject<T>(name = name)
        protected inline fun <reified T : Any> injectServiceMap() = strong.injectServiceMap<T>()
        protected inline fun <reified T : Any> injectServiceList() = strong.injectServiceList<T>()
        protected inline fun <reified T : Any> injectOrNull(name: String? = null) = strong.injectOrNull<T>(name = name)

        private var inits: MutableList<LazyInitPropertyDelegateProvider<*>>? = null
        private var links: MutableList<LazyInitPropertyDelegateProvider<*>>? = null
        private var inited = false
        private var linked = false
        protected fun <T> onInit(func: suspend () -> T): PropertyDelegateProvider<Bean, ReadOnlyProperty<Bean, T>> {
            if (inited) {
                throw IllegalStateException("Can't create onInit property. Init phase already finished")
            }
            val delegateProvider = LazyInitPropertyDelegateProvider(func)
            if (inits == null) {
                inits = defaultMutableList2()
            }
            inits!!.add(delegateProvider)
            return delegateProvider
        }

        protected fun <T> onLink(func: suspend () -> T): PropertyDelegateProvider<Bean, ReadOnlyProperty<Bean, T>> {
            if (linked) {
                throw IllegalStateException("Can't create onLink property. Link phase already finished")
            }
            val delegateProvider = LazyInitPropertyDelegateProvider(func)
            if (links == null) {
                links = defaultMutableList2()
            }
            links!!.add(delegateProvider)
            return delegateProvider
        }

        override suspend fun init(strong: Strong) {
            inited = true
            inits?.forEach {
                try {
                    it.delegator.initValue()
                } catch (e: Throwable) {
                    throw StrongException("Can't init ${this::class}.${it.delegator.fieldName}", e)
                }
            }
            inits?.clear()
            inits = null
        }

        override suspend fun link(strong: Strong) {
            linked = true
            links?.forEach {
                try {
                    it.delegator.initValue()
                } catch (e: Throwable) {
                    throw StrongException("Can't init ${this::class}.${it.delegator.fieldName}", e)
                }
            }
            links?.clear()
            links = null
        }
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

        suspend fun create(vararg config: Config): Strong {
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

    fun <T : Any> service(beanClass: KClass<T>, name: String? = null): ServiceProvider<T>
    fun <T : Any> serviceMap(beanClass: KClass<T>): ServiceProvider<Map<String, T>>
    fun <T : Any> serviceList(beanClass: KClass<T>): ServiceProvider<List<T>>
    fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String? = null): ServiceProvider<T?>
    suspend fun destroy()
    val isDestroying: Boolean
    val isDestroyed: Boolean

    //    fun interrupt()
    suspend fun awaitDestroy()

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

    @Deprecated("Not Use it. It will be deleted")
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

suspend fun Strong.Companion.launch(vararg config: Strong.Config, afterInit: ((Strong) -> Unit)? = null) {
    val strong = create(
        *config
    )
    if (afterInit != null) {
        afterInit(strong)
    }
    strong.awaitDestroy()
}
