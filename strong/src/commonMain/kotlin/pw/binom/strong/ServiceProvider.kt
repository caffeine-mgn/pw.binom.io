package pw.binom.strong

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ServiceProvider<T> : ReadOnlyProperty<Any?, T>, Lazy<T> {
  val service: T

  companion object {
    fun <T> provide(value: T) = object : ServiceProvider<T> {
      override val service: T
        get() = value
      override val value: T
        get() = service

      override fun isInitialized(): Boolean = true
    }

    fun <T> provide(value: () -> T) = object : ServiceProvider<T> {
      private val initingLock = SpinLock()
      private var inited = false
      private var data: T? = null
      private var exception: Throwable? = null

      @Suppress("UNCHECKED_CAST")
      override val service: T
        get() = initingLock.synchronize {
          if (inited) {
            val exception = exception
            val data = data
            if (exception != null) {
              throw exception
            } else {
              return data as T
            }
          } else {
            try {
              val tmp = value()
              this.data = tmp
              inited = true
              return tmp
            } catch (e: Throwable) {
              this.exception = e
              inited = true
              throw e
            }
          }
        }
      override val value: T
        get() = service

      override fun isInitialized(): Boolean = initingLock.synchronize {
        inited
      }
    }
  }

  override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
    service
}

fun <FROM, TO> ServiceProvider<FROM>.map(func: (FROM) -> TO): ServiceProvider<TO> =
  object : ServiceProvider<TO> {
    override val service: TO
      get() = func(this@map.service)
    override val value: TO
      get() = service

    override fun isInitialized(): Boolean = this@map.isInitialized()
  }
