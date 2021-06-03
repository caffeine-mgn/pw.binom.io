package pw.binom.concurrency

import kotlinx.cinterop.StableRef
import pw.binom.doFreeze
import pw.binom.io.Closeable


actual class Reference<T : Any?> actual constructor(value: T) : Closeable {
    private val ptr = value?.let { StableRef.create(it) }
    actual val owner = ThreadRef()
    @Suppress("UNCHECKED_CAST")
    actual val value: T
        get() {
            if (!owner.same)
                throw IllegalStateException("Can't get access to value from other thread")
            return ptr?.get() as T
        }

    override fun close() {
        ptr?.dispose()
    }

    init {
        doFreeze()
    }
}