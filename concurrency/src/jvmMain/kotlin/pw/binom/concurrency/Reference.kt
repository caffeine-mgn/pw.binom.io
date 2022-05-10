package pw.binom.concurrency

import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

actual class Reference<T : Any?> actual constructor(value: T) : Closeable, ReadOnlyProperty<Any?, T> {
    actual val owner = ThreadRef()

    @Volatile
    private var closed = false

    actual val value: T = value
        @JvmName("value2")
        get() {
            if (closed) {
                throw ClosedException()
            }
            if (!owner.same)
                throw IllegalStateException("Can't get access to value from other thread")
            return field
        }

    override fun close() {
        if (closed) {
            throw ClosedException()
        }
        closed = true
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value
}
