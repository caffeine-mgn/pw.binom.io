package pw.binom.collections

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual class WeakReference<T : Any> actual constructor(value: T) {

  val native = kotlin.native.ref.WeakReference(value)

  actual val get: T?
    get() = native.get()
}
