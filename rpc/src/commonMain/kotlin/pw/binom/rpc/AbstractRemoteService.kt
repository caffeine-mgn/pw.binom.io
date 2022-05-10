package pw.binom.rpc

import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

abstract class AbstractRemoteService(val invoker: Invoker) : CrossService {
    interface Invoker {
        suspend fun invoke(service: String, method: String, params: Map<String, Any?>): Any?
    }

    inner class RemoteMethod<T> internal constructor(val name: String) : CrossService.CrossMethod<T> {
        override suspend operator fun invoke(params: Map<String, Any?>): T =
            invoker.invoke(this@AbstractRemoteService.name, name, params) as T

        override fun getValue(thisRef: Any, property: KProperty<*>): CrossService.CrossMethod<T> = this
    }

    inner class Provider<T> : PropertyDelegateProvider<Any, RemoteMethod<T>> {
        override fun provideDelegate(thisRef: Any, property: KProperty<*>): RemoteMethod<T> =
            RemoteMethod(property.name)
    }

    protected fun <T> remote() = Provider<T>()
}
