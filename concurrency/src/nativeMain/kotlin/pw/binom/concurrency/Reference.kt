package pw.binom.concurrency

import kotlinx.cinterop.StableRef
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import kotlin.native.concurrent.AtomicInt
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

actual class Reference<T : Any?> actual constructor(value: T) : Closeable, ReadOnlyProperty<Any?, T> {
    private val ptr = value?.let { StableRef.create(it) }
    private var closed = AtomicInt(0)
    actual val owner = ThreadRef()

    @Suppress("UNCHECKED_CAST")
    actual val value: T
        get() {
            if (closed.value != 0) {
                throw ClosedException()
            }
            if (!owner.same)
                throw IllegalStateException("Can't get access to value from other thread")
            return ptr?.get() as T
        }

    override fun close() {
        if (closed.value != 0) {
            throw ClosedException()
        }
        closed.value = 1
        ptr?.dispose()
    }

    init {
        doFreeze()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value
}
