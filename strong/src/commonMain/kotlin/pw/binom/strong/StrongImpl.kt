package pw.binom.strong

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.logger.Logger
import pw.binom.logger.debug
import pw.binom.logger.severe
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class StrongImpl : Strong {
    private val logger = Logger.getLogger("Strong.Starter")
    internal var beans = HashMap<String, BeanEntity>()
    internal lateinit var destroyOrder: List<Strong.DestroyableBean>

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
        logger.debug("start destroy")
        check(!isDestroyed) { "Strong already isDestroyed" }
        check(!isDestroying) { "Strong already destroying" }
        isDestroying = true
        try {
            destroyOrder.forEach {
                try {
                    logger.debug("Destroying $it")
                    it.destroy(this)
                    logger.debug("Destroyed $it success")
                } catch (e: Throwable) {
                    logger.severe("Fail to destroy $it bean", e)
                }
            }
            interruptingListenersLock.synchronize {
                interruptingListeners.forEach {
                    it.resume(Unit)
                }
            }
        } catch (e: Throwable) {
            interruptingListenersLock.synchronize {
                interruptingListeners.forEach {
                    it.resumeWithException(e)
                }
            }
            logger.severe("Fail to destroy", e)
            throw e
        } finally {
            isDestroying = false
            isDestroyed = true
        }
    }

    override var isDestroying: Boolean = false
        private set
    override var isDestroyed: Boolean = false
        private set

    private val interruptingListenersLock = SpinLock()
    private val interruptingListeners = ArrayList<Continuation<Unit>>()

    override suspend fun awaitDestroy() {
        if (isDestroyed) {
            return
        }
        suspendCoroutine<Unit> {
            interruptingListenersLock.synchronize {
                interruptingListeners += it
            }
        }
    }

    override fun contains(beanName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : Any> contains(clazz: KClass<T>): Boolean {
        TODO("Not yet implemented")
    }
}
