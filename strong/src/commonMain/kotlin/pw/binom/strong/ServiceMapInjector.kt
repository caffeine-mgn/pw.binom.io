package pw.binom.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
internal class ServiceMapInjector<T : Any>(val strong: StrongImpl, val beanClass: KClass<T>) :
  ServiceProvider<Map<String, T>> {
  private val map = lazy {
    strong.findBean(beanClass as KClass<Any>, null).map { it.key to it.value.bean as T }
      .toMap()
  }
  override val service: Map<String, T>
    get() = map.value

  override operator fun getValue(thisRef: Any?, property: KProperty<*>): Map<String, T> =
    service

  override val value: Map<String, T>
    get() = service

  override fun isInitialized(): Boolean {
    TODO("Not yet implemented")
  }
}
