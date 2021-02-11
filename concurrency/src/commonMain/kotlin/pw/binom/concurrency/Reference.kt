package pw.binom.concurrency

import pw.binom.io.Closeable

/**
 * Wrapper for [value]. Wrapped object can be pass from creator thread to other thread.
 * But get [value] can only thread creator
 */
expect class Reference<T : Any?>(value: T) : Closeable {
    /**
     * Thread creator
     */
    val owner: ThreadRef
    val value: T
}

fun <T : Any> T.asReference() = Reference(this)
fun <T : Any?> Reference<T>.free(): T {
    val result = value
    close()
    return result
}