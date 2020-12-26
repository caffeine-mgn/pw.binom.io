package pw.binom.strong

import pw.binom.uuid
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Strong private constructor() {
    private val beans = HashMap<String, Any>()
    private var inited = false
    private var initing = false

    interface PropertyProvider {
        suspend fun init(): Map<String, String>
    }

    interface Config {
        suspend fun apply(strong: Strong)
    }

    interface InitializingBean {
        suspend fun init()
    }

    interface LinkingBean {
        suspend fun link()
    }

    interface ServiceProvider {
        suspend fun provide()
    }

    private var properties = HashMap<String, String>()

    companion object {

        suspend fun create(vararg config: Config): Strong {
            val strong = Strong()
            config.forEach {
                it.apply(strong)
            }


            return strong
        }

        fun config(func: (Strong) -> Unit) = object : Config {
            override suspend fun apply(strong: Strong) {
                func(strong)
            }
        }
    }

    suspend fun start() {
        if (inited) {
            throw IllegalStateException("Strong already started")
        }
        initing = true
        beanOrder.forEach {
            if (it is PropertyProvider) {
                properties.putAll(it.init())
            }
        }

        beanOrder.forEach {
            if (it is ServiceProvider) {
                it.provide()
            }
        }
        beanOrder.forEach {
            if (it is InitializingBean) {
                try {
                    it.init()
                } catch (e: Throwable) {
                    throw RuntimeException("Can't init bean ${it::class}", e)
                }
            }
        }
        beanOrder.forEach {
            if (it is LinkingBean) {
                it.link()
            }
        }
        beanOrder.clear()
        inited = true
        initing = false
    }

    class BeanAlreadyDefinedException(val beanName: String) : RuntimeException() {
        override val message: String?
            get() = "Bean \"$beanName\" already defined"
    }

    class NoSuchBeanException(val clazz: KClass<out Any>) : RuntimeException() {
        override val message: String?
            get() = "Bean ${clazz} not found"
    }

    class SeveralBeanException(val clazz: KClass<out Any>, val name: String?) : RuntimeException() {
        override val message: String?
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
     * Returns true if bean with [name] already defined
     *
     * @return true if bean with [name] already defined
     */
    fun exist(name: String) = beans.containsKey(name)

    /**
     * Define [bean]. Default [name] is `[bean]::class + "_" + [bean].class.hashCode()`
     *
     * @param bean object for define
     * @param name name of [bean] for define. See description of method for get default value
     * @param ifNotExist if false on duplicate will throw [BeanAlreadyDefinedException]. If true will ignore redefine
     */
    fun define(bean: Any, name: String = "${bean::class}_${bean::class.hashCode()}", ifNotExist: Boolean = false) {
        if (inited) {
            throw IllegalStateException("Strong already inited")
        }
        if (initing){
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

    fun <T : Any> serviceList(beanClass: KClass<T>) = ServiceListInjector(this, beanClass)
    inline fun <reified T : Any> serviceList() = serviceList(T::class)

    fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String? = null) =
        NullableServiceInjector(this, beanClass, name)

    inline fun <reified T : Any> serviceOrNull(name: String? = null) = serviceOrNull(T::class, name)
    fun property(name: String) = StringProperty(this, name)

    abstract class AbstractPropertyInjector<T : Any?> internal constructor(
        val strong: Strong,
        val name: String
    ) : ReadOnlyProperty<Any?, T> {
        internal fun getString() = strong.properties[name]
    }

    class MappedProperty<FROM, TO>(val provider: () -> FROM, val mapper: (FROM) -> TO) : ReadOnlyProperty<Any?, TO> {
        private var inited = false
        private var value: TO? = null
        override fun getValue(thisRef: Any?, property: KProperty<*>): TO {
            if (!inited) {
                value = mapper(provider())
                inited = true
            }
            return value as TO
        }
    }

    class StringProperty(
        strong: Strong,
        name: String,
    ) : AbstractPropertyInjector<String?>(strong = strong, name = name) {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = getString()
        fun default(value: String) = StringPropertyWithDefault(
            property = this,
            default = value
        )

        fun <T> map(mapper: (String?) -> T) = MappedProperty(
            provider = { getString() },
            mapper = mapper
        )
    }

    class StringPropertyWithDefault(val property: StringProperty, var default: String) :
        ReadOnlyProperty<Any?, String> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String =
            this.property.getValue(thisRef, property) ?: default

        fun <T> map(mapper: (String) -> T) = MappedProperty(
            provider = { property.getString() ?: default },
            mapper = mapper
        )
    }

    abstract class AbstractServiceInjector<T : Any> internal constructor(
        val strong: Strong,
        val beanClass: KClass<T>,
        val name: String?
    ) {
        private var bean: T? = null

        protected val value: T?
            get() {
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
                return bean
            }
    }

    class ServiceInjector<T : Any>(strong: Strong, beanClass: KClass<T>, name: String?) : AbstractServiceInjector<T>(
        strong = strong,
        beanClass = beanClass,
        name = name
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
            ?: throw NoSuchBeanException(beanClass)
    }

    class ServiceListInjector<T : Any>(val strong: Strong, val beanClass: KClass<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Map<String, T> {
            return strong.beans.asSequence().filter {
                beanClass.isInstance(it.value)
            }
                .map { it.key to it.value as T }
                .toMap()
        }
    }

    class NullableServiceInjector<T : Any>(strong: Strong, beanClass: KClass<T>, name: String?) :
        AbstractServiceInjector<T>(
            strong = strong,
            beanClass = beanClass,
            name = name
        ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value
    }
}