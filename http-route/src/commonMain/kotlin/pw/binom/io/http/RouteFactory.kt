package pw.binom.io.http

import kotlin.reflect.KClass

object ClientFactory {
  fun <T : Any> generate(config: ClientRouteConfig): T = TODO()
}

interface RouteFactory<T : Any> {

  companion object {
    private val factories = HashMap<KClass<*>, RouteFactory<Any>>()
    internal fun reg(clazz: KClass<Any>, factory: RouteFactory<Any>) {
      factories[clazz] = factory
    }

    internal fun aaa() {
      println("REG!")
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> getRouteFactoryOrNull(clazz: KClass<T>) = factories[clazz] as RouteFactory<T>?
  }

  val clientClass: KClass<T>
    get() = throw IllegalStateException("Not implemented")

  fun create(config: ClientRouteConfig): T = throw IllegalStateException("Not implemented")
}
