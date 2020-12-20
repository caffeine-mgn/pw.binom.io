package pw.binom.concurrency

import pw.binom.io.Closeable

actual class Reference<T : Any?> actual constructor(value: T) : Closeable {
    actual val owner = ThreadRef()
    actual val value: T = value
        get() {
            if (!owner.same)
                throw IllegalStateException("Can't get access to value from other thread")
            return field
        }

    override fun close() {
    }
}