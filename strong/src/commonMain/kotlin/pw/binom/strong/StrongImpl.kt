package pw.binom.strong

import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class StrongImpl internal constructor() : StrongDefiner {
    internal val beans = HashMap<String, Any>()
    private var inited = false
    private var initing = false

    internal suspend fun start() {
        if (inited) {
            throw IllegalStateException("Strong already started")
        }
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

    private val beanOrder = ArrayList<Any>()

    override fun <T : Any> contains(clazz: KClass<T>) = contains("${clazz}_${clazz.hashCode()}")

    override fun contains(beanName: String) = beans.containsKey(beanName)

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

    override fun <T : Any> service(beanClass: KClass<T>, name: String?) = ServiceInjector(this, beanClass, name)
    override fun <T : Any> serviceMap(beanClass: KClass<T>) = ServiceMapInjector(this, beanClass)
    override fun <T : Any> serviceList(beanClass: KClass<T>) = ServiceListInjector(this, beanClass)
    override fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String?) =
        NullableServiceInjector(this, beanClass, name)
}