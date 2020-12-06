package pw.binom.strong

import pw.binom.uuid
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Strong private constructor() {
    private val beans = HashMap<String, Any>()
    private var inited = false

    interface PropertyProvider {
        suspend fun init(): Map<String, String>
    }

    interface Config {
        fun apply(strong: Strong)
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

        fun create(vararg config: Config): Strong {
            val strong = Strong()
            config.forEach {
                it.apply(strong)
            }


            return strong
        }

        fun config(func: (Strong) -> Unit) = object : Config {
            override fun apply(strong: Strong) {
                func(strong)
            }
        }
    }

    suspend fun start() {
        if (inited) {
            throw IllegalStateException("Strong already started")
        }
        beans.values.forEach {
            if (it is PropertyProvider) {
                properties.putAll(it.init())
            }
        }

        beans.values.forEach {
            if (it is ServiceProvider) {
                it.provide()
            }
        }
        beans.values.forEach {
            if (it is InitializingBean) {
                try {
                    it.init()
                } catch (e: Throwable) {
                    throw RuntimeException("Can't init bean ${it::class}", e)
                }
            }
        }
        beans.values.forEach {
            if (it is LinkingBean) {
                it.link()
            }
        }
        inited = true
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

    fun define(bean: Any, name: String = Random.uuid().toString()) {
        if (inited) {
            throw IllegalStateException("Strong already inited")
        }
        if (beans.containsKey(name))
            throw BeanAlreadyDefinedException(name)
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