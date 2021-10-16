package pw.binom.concurrency

import pw.binom.io.Closeable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Wrapper for [value]. Wrapped object can be pass from creator thread to other thread.
 * But get [value] can only thread creator.
 * Also you can create [Reference] in one thread and then close in other thread
 *
 * Remember: you must call [close] for fix memory leaks. Or use [useReference]
 */
expect class Reference<T : Any?>(value: T) : Closeable, ReadOnlyProperty<Any?, T> {
    /**
     * Thread creator
     */
    val owner: ThreadRef
    val value: T
//    operator fun getValue(thisRef: T, property: KProperty<*>): T
}

fun <T : Any> T.asReference() = Reference(this)

/**
 * Creates [Reference] for current this object. Then calls [block] with pass created [Reference].
 * After call [block] closes created [Reference]
 */
inline fun <T : Any, R> T.useReference(block: (Reference<T>) -> R): R {
    val r = Reference(this)
    return try {
        block(r)
    } finally {
        r.close()
    }
}

fun <T : Any?> Reference<T>.free(): T {
    val result = value
    close()
    return result
}