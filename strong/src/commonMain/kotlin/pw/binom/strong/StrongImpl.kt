package pw.binom.strong

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class StrongImpl internal constructor() : StrongDefiner {
    private val beans = HashMap<String, Any>()
    private var inited = false
    private var initing = false

//    interface PropertyProvider {
//        suspend fun readProperties(): Map<String, String>
//    }
//
//    private var properties = HashMap<String, String>()

    internal suspend fun start() {
        if (inited) {
            throw IllegalStateException("Strong already started")
        }
//        beanOrder.forEach {
//            if (it is PropertyProvider) {
//                properties.putAll(it.readProperties())
//            }
//        }
        ArrayList(beanOrder).forEach {
            if (it is Strong.ServiceProvider) {
                it.provide(this)
            }
        }
        initing = true
        beanOrder.forEach {
            if (it is Strong.InitializingBean) {
                try {
                    it.init(this)
                } catch (e: Throwable) {
                    throw RuntimeException("Can't init bean ${it::class}", e)
                }
            }
        }
        beanOrder.forEach {
            if (it is Strong.LinkingBean) {
                it.link(this)
            }
        }
        beanOrder.clear()
        inited = true
        initing = false
    }

    suspend fun destroy() {
        beans.values.forEach {
            if (it is Strong.DestroyableBean) {
                it.destroy(this)
            }
        }
        beans.clear()
    }

    class BeanAlreadyDefinedException(val beanName: String) : RuntimeException() {
        override val message: String
            get() = "Bean \"$beanName\" already defined"
    }

    class NoSuchBeanException(val clazz: KClass<out Any>) : RuntimeException() {
        override val message: String
            get() = "Bean ${clazz} not found"
    }

    class SeveralBeanException(val clazz: KClass<out Any>, val name: String?) : RuntimeException() {
        override val message: String
            get() = if (name != null) {
                "Several bean $clazz with name $name"
            } else {
                "Several bean $clazz"
            }
    }

    private val beanOrder = ArrayList<Any>()

    /**
     * Returns true if bean with class [klass] with default name already defined
     *
     * @return true if bean of class [klass] defined with default name
     */
    fun <T : Any> exist(klass: KClass<T>) = exist("${klass}_${klass.hashCode()}")

    /**
     * Returns true if bean with class [T] with default name already defined
     *
     * @return true if bean of class [T] defined with default name
     */
    inline fun <reified T : Any> exist() = exist(T::class)

    /**
     * Returns true if bean with [name] already defined
     *
     * @return true if bean with [name] already defined
     */
    fun exist(name: String) = beans.containsKey(name)

    override fun define(bean: Any, name: String, ifNotExist: Boolean) {
        if (inited) {
            throw IllegalStateException("Strong already inited")
        }
        if (initing) {
            throw IllegalStateException("Can't define bean during start process")
        }
        if (beans.containsKey(name)) {
            if (ifNotExist) {
                return
            } else {
                throw BeanAlreadyDefinedException(name)
            }
        }
        beanOrder += bean
        beans[name] = bean
    }

    fun <T : Any> service(beanClass: KClass<T>, name: String? = null) = ServiceInjector(this, beanClass, name)
    inline fun <reified T : Any> service(name: String? = null) = service(T::class, name)

    fun <T : Any> serviceMap(beanClass: KClass<T>) = ServiceMapInjector(this, beanClass)
    inline fun <reified T : Any> serviceMap() = serviceMap(T::class)

    fun <T : Any> serviceList(beanClass: KClass<T>) = ServiceListInjector(this, beanClass)
    inline fun <reified T : Any> serviceList() = serviceList(T::class)

    fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String? = null) =
        NullableServiceInjector(this, beanClass, name)

    inline fun <reified T : Any> serviceOrNull(name: String? = null) = serviceOrNull(T::class, name)

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

    class ServiceInjector<T : Any>(strong: StrongImpl, beanClass: KClass<T>, name: String?) :
        AbstractServiceInjector<T>(
            strong = strong,
            beanClass = beanClass,
            name = name
        ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
            ?: throw NoSuchBeanException(beanClass)
    }

    class ServiceMapInjector<T : Any>(val strong: StrongImpl, val beanClass: KClass<T>) {
        private val map by lazy {
            strong.beans.asSequence().filter {
                beanClass.isInstance(it.value)
            }
                .map { it.key to it.value as T }
                .toMap()
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Map<String, T> =
            map
    }

    class ServiceListInjector<T : Any>(val strong: StrongImpl, val beanClass: KClass<T>) {
        private val list by lazy {
            strong.beans.asSequence().filter {
                beanClass.isInstance(it.value)
            }.map { it.value as T }.toList()
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> =
            list
    }

    class NullableServiceInjector<T : Any>(strong: StrongImpl, beanClass: KClass<T>, name: String?) :
        AbstractServiceInjector<T>(
            strong = strong,
            beanClass = beanClass,
            name = name
        ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value
    }
}