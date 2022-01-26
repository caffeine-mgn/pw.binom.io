package pw.binom

import kotlin.jvm.JvmName

expect class WeakReference<T : Any> {
    constructor(value: T)

    val get: T?
}
