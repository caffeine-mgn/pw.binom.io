package pw.binom.concurrency

import pw.binom.Future
import pw.binom.io.Closeable

expect class StateHolder:Closeable {
    fun <T : Any> make(value: T): Future<Reference<T>>
    fun <T : Any,R> access(value: Reference<T>, func: (T) -> R):Future<R>
}