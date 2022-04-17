package pw.binom

actual class WeakReference<T : Any> actual constructor(value: T) {

    val native = JSWeakRef(value)

    actual val get: T?
        get() = native.deref()
}
