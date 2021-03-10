package pw.binom.strong

import pw.binom.io.Closeable
import kotlin.reflect.KClass

class EventSystem {

    private val listeners = HashMap<KClass<Any>, ArrayList<suspend (Any) -> Unit>>()

    inline fun <reified T : Any> listen(noinline listener: suspend (T) -> Unit): Closeable = listen(T::class, listener)

    fun <T : Any> listen(objectClass: KClass<T>, listener: suspend (T) -> Unit): Closeable =
        run {
            listeners.getOrPut(objectClass as KClass<Any>) { ArrayList() }.add(listener as suspend (Any) -> Unit)
            Closeable {
                val list = listeners[objectClass]
                if (list != null) {
                    list.remove(listener)
                    if (list.isEmpty()) {
                        listeners.remove(list)
                    }
                }
            }
        }

    suspend fun dispatch(eventObject: Any) {
        val listeners = run {
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