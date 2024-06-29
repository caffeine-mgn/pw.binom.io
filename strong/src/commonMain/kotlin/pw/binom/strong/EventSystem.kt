package pw.binom.strong

import pw.binom.io.Closeable
import kotlin.reflect.KClass

interface EventSystem {
  fun <T : Any> listen(objectClass: KClass<T>, listener: suspend (T) -> Unit): Closeable
  suspend fun dispatch(eventObject: Any)
}
inline fun <reified T : Any> EventSystem.listen(noinline listener: suspend (T) -> Unit): Closeable = listen(T::class, listener)

inline fun <reified T : Any> EventSystem.once(noinline listener: suspend (T) -> Unit): Closeable {
  var closable: Closeable? = null
  closable = listen(T::class) {
    try {
      listener(it)
    } finally {
      closable?.close()
    }
  }
  return closable
}

inline fun <reified T : Any> EventSystem.listenUntil(noinline listener: suspend (T) -> Boolean): Closeable {
  var closable: Closeable? = null
  closable = listen(T::class) {
    if (!listener(it)) {
      closable?.close()
    }
  }
  return closable
}
