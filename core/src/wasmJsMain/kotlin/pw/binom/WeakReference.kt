package pw.binom

actual class WeakReference<T : Any> actual constructor(value: T) {

  val native = JSWeakRef(value.toJsReference())

  actual val get: T?
    get() = native.deref() as T?
}

@JsName("WeakRef")
external class JSWeakRef:JsAny {
  constructor(value: JsReference<Any>)

  fun deref(): JsReference<Any>
}
