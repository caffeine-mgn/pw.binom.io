package pw.binom.strong

import kotlin.reflect.KClass

internal class StrongImpl : Strong {

    internal var beans = HashMap<String, BeanEntity>()

    sealed class Dependency {
        class ClassDependency(val clazz: KClass<Any>, val name: String?, val require: Boolean) : Dependency()
        class ClassSetDependency(val clazz: KClass<Any>) : Dependency()
    }

    private val internalDependencies = ArrayList<Dependency>()
    val dependencies: List<Dependency>
        get() = internalDependencies

    fun defining() {
        internalDependencies.clear()
    }
    fun findBean(clazz: KClass<Any>, name: String?) =
        beans.asSequence().filter {
            clazz.isInstance(it.value.bean) && (name == null || it.key == name)
        }

    override fun <T : Any> service(beanClass: KClass<T>, name: String?): ServiceProvider<T> {
        internalDependencies += Dependency.ClassDependency(beanClass as KClass<Any>, name, true)
        return ServiceInjector(this, beanClass, name)
    }

    override fun <T : Any> serviceMap(beanClass: KClass<T>): ServiceProvider<Map<String, T>> {
        internalDependencies += Dependency.ClassSetDependency(beanClass as KClass<Any>)
        return ServiceMapInjector(this, beanClass)
    }

    override fun <T : Any> serviceList(beanClass: KClass<T>): ServiceProvider<List<T>> {
        internalDependencies += Dependency.ClassSetDependency(beanClass as KClass<Any>)
        return ServiceListInjector(this, beanClass)
    }

    override fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String?): ServiceProvider<T?> {
        internalDependencies += Dependency.ClassDependency(beanClass as KClass<Any>, name, true)
        return NullableServiceInjector(this, beanClass, name)
    }

    override suspend fun destroy() {
        for (i in beanOrder.size - 1 downTo 0) {
            val bean = beanOrder[i].second
            if (bean is Strong.DestroyableBean) {
                bean.destroy(this)
            }
        }
    }

    override fun contains(beanName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : Any> contains(clazz: KClass<T>): Boolean {
        TODO("Not yet implemented")
    }

    internal lateinit var beanOrder: List<Pair<String, Any>>
}