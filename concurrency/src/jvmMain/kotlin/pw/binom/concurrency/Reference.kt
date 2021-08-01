package pw.binom.concurrency

import pw.binom.io.Closeable
import pw.binom.io.ClosedException

actual class Reference<T : Any?> actual constructor(value: T) : Closeable {
    actual val owner = ThreadRef()

    @Volatile
    private var closed = false

    actual val value: T = value
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
}