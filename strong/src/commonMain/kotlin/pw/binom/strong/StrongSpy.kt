package pw.binom.strong

import kotlin.reflect.KClass

internal class StrongSpy : Strong {
  private val internalDependencies = ArrayList<BeanDependency>()
  val dependencies: List<BeanDependency>
    get() = internalDependencies

  override fun <T : Any, R : T> overrideBean(oldBean: T, newBean: R): Boolean {
    TODO("Not yet implemented")
  }

  class InjectPoint<T> : ServiceProvider<T> {
    private var inited = false
    fun init(value: T) {
      injected = value
      inited = true
    }

    private var injected: T? = null
    override val service: T
      get() = if (inited) injected as T else throw IllegalStateException("Bean not initialized")
    override val value: T
      get() = service

    override fun isInitialized(): Boolean = inited

  }

  interface BeanDependency {
    data class Single<T : Any>(
      val beanClass: KClass<T>,
      val name: String?,
      val optional: Boolean,
      val injectPoint: InjectPoint<T?>,
    ) : BeanDependency

    data class Map<T : Any>(val beanClass: KClass<T>, val injectPoint: InjectPoint<kotlin.collections.Map<String, T>>) :
      BeanDependency

    data class List<T : Any>(val beanClass: KClass<T>, val injectPoint: InjectPoint<kotlin.collections.List<T>>) :
      BeanDependency
  }

  override fun <T : Any> service(beanClass: KClass<T>, name: String?): ServiceProvider<T> {
    val ip = InjectPoint<T>()
    BeanDependency.Single(beanClass = beanClass, name = name, optional = true, injectPoint = ip as InjectPoint<T?>)
    return ip
  }

  override fun <T : Any> serviceMap(beanClass: KClass<T>): ServiceProvider<Map<String, T>> {
    val ip = InjectPoint<Map<String, T>>()
    BeanDependency.Map(beanClass = beanClass, injectPoint = ip)
    return ip
  }

  override fun <T : Any> serviceList(beanClass: KClass<T>): ServiceProvider<List<T>> {
    val ip = InjectPoint<List<T>>()
    BeanDependency.List(beanClass = beanClass, injectPoint = ip)
    return ip
  }

  override fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String?): ServiceProvider<T?> {
    val ip = InjectPoint<T?>()
    BeanDependency.Single(beanClass = beanClass, name = name, optional = true, injectPoint = ip)
    return ip
  }

  override suspend fun destroy() {
    TODO("Not yet implemented")
  }

  override val isDestroying: Boolean
    get() = TODO("Not yet implemented")
  override val isDestroyed: Boolean
    get() = TODO("Not yet implemented")

  override suspend fun awaitDestroy() {
    TODO("Not yet implemented")
  }

  override fun contains(beanName: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun <T : Any> contains(clazz: KClass<T>): Boolean {
    TODO("Not yet implemented")
  }
}
