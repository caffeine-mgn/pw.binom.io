package pw.binom.concurrency

import kotlinx.cinterop.StableRef
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import kotlin.native.concurrent.AtomicInt


actual class Reference<T : Any?> actual constructor(value: T) : Closeable {
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
}