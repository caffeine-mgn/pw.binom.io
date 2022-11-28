package pw.binom.cleaner

@JsName("WeakRef")
internal external class JSWeakRef {
    constructor(value: dynamic)

    fun deref(): dynamic
}
