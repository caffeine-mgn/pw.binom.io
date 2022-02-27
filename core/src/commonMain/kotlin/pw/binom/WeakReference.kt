package pw.binom

expect class WeakReference<T : Any> {
    constructor(value: T)

    val get: T?
}
