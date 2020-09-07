package pw.binom.concurrency

import pw.binom.Future
import pw.binom.io.Closeable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

expect class StateHolder : Closeable {
    constructor()
    fun <T : Any> make(value: () -> T): Future<Reference<T>>
    fun <T : Any, R> access(value: Reference<T>, func: (T) -> R): Future<R>
}

fun <T : Any> StateHolder.stateOf(func: () -> T) = State(this, func)

class State<T : Any>(val holder: StateHolder, value: () -> T) {
    private val ref = holder.make(value).resultOrNull!!
    fun <R> access(func: (T) -> R) = holder.access(ref, func)

}