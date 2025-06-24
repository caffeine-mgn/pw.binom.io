package pw.binom.rpc

import kotlinx.serialization.KSerializer
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
abstract class AbstractRemoteService(val invoker: Invoker) : CrossService {
  interface Invoker {
    suspend fun invoke(service: String, method: String, params: Any?): Any?
  }

  inner class RemoteMethod<REQUEST,RESPONSE> internal constructor(val name: String) : CrossService.CrossMethod<REQUEST,RESPONSE> {
    override suspend operator fun invoke(params: REQUEST): RESPONSE =
      invoker.invoke(this@AbstractRemoteService.name, name, params as Any?) as RESPONSE

    override fun getValue(thisRef: Any, property: KProperty<*>): CrossService.CrossMethod<REQUEST,RESPONSE> = this
  }

  inner class Provider<REQUEST,RESPONSE> : PropertyDelegateProvider<Any, RemoteMethod<REQUEST,RESPONSE>> {
    override fun provideDelegate(thisRef: Any, property: KProperty<*>): RemoteMethod<REQUEST,RESPONSE> =
      RemoteMethod(property.name)
  }

  protected fun <REQUEST,RESPONSE> remote(k:KSerializer) = Provider<REQUEST,RESPONSE>()
}
