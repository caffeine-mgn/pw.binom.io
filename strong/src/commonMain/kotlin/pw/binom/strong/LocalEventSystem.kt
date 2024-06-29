package pw.binom.strong

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class LocalEventSystem : EventSystem {
  private val listLock = SpinLock()
  private val listeners =
    defaultMutableMap<KClass<Any>, MutableList<suspend (Any) -> Unit>>().useName("EventSystem.listeners")

  override fun <T : Any> listen(objectClass: KClass<T>, listener: suspend (T) -> Unit): Closeable =
    run {
      objectClass as KClass<Any>
      listener as suspend (Any) -> Unit
      listLock.synchronize {
        listeners.getOrPut(objectClass) { defaultMutableList() }.add(listener)
      }
      Closeable {
        listLock.synchronize {
          val list = listeners[objectClass]
          if (list != null) {
            list.remove(listener)
            if (list.isEmpty()) {
              listeners.remove(objectClass)
            }
          }
        }
      }
    }

  override suspend fun dispatch(eventObject: Any) {
    val listeners = listLock.synchronize {
      listeners.asSequence()
        .filter { it.key.isInstance(eventObject) }
        .flatMap { it.value.asSequence() }
        .toList()
    }
    listeners.forEach {
      it(eventObject)
    }
  }
}
