package pw.binom

actual class WeakReference<T : Any> actual constructor(value: T) {

  actual val get: T?
    get() = TODO()
}
