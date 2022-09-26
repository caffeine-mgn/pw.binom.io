package pw.binom.strong

import pw.binom.collections.defaultArrayList
import pw.binom.collections.defaultHashMap
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class EventSystem {
    private val listLock = SpinLock()
    private val listeners = defaultHashMap<KClass<Any>, MutableList<suspend (Any) -> Unit>>()
    inline fun <reified T : Any> listen(noinline listener: suspend (T) -> Unit): Closeable = listen(T::class, listener)
    inline fun <reified T : Any> once(noinline listener: suspend (T) -> Unit): Closeable {
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

    fun <T : Any> listen(objectClass: KClass<T>, listener: suspend (T) -> Unit): Closeable =
        run {
            objectClass as KClass<Any>
            listener as suspend (Any) -> Unit
            listLock.synchronize {
                listeners.getOrPut(objectClass) { defaultArrayList() }.add(listener)
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

    suspend fun dispatch(eventObject: Any) {
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
