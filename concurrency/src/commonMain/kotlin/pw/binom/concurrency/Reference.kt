package pw.binom.concurrency

import pw.binom.io.Closeable

expect class Reference<T : Any?>(value: T) : Closeable {
    val owner: ThreadRef
    val value: T
}

fun <T : Any?> T.asReference() = Reference(this)