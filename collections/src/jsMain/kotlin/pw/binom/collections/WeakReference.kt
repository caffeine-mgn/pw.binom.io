package pw.binom.collections

internal actual class WeakReference<T : Any> actual constructor(value: T) {

    val native = JSWeakRef(value)

    actual val get: T?
        get() = native.deref()
}

@JsName("WeakRef")
internal external class JSWeakRef {
    constructor(value: dynamic)

    fun deref(): dynamic
}
