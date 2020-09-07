package pw.binom.strong

import pw.binom.io.Closeable
import pw.binom.concurrency.Lock
import pw.binom.concurrency.synchronize
import kotlin.reflect.KClass

class EventSystem {

    private val lock = Lock()
    private val listeners = HashMap<KClass<Any>, ArrayList<(Any) -> Unit>>()

    inline fun <reified T : Any> listen(noinline listener: (T) -> Unit): Closeable = listen(T::class, listener)

    fun <T : Any> listen(objectClass: KClass<T>, listener: (T) -> Unit): Closeable =
            lock.synchronize {
                listeners.getOrPut(objectClass as KClass<Any>) { ArrayList() }.add(listener as (Any) -> Unit)
                Closeable {
                    lock.synchronize {
                        listeners.get(objectClass as KClass<Any>)?.remove(listener)
                    }
                }
            }

    fun dispatch(dto: Any) {
        val listeners = lock.synchronize {
            listeners.asSequence()
                    .filter { it.key.isInstance(dto) }
                    .flatMap { it.value.asSequence() }
                    .toList()
        }
        listeners.forEach {
            it(dto)
        }
    }
}