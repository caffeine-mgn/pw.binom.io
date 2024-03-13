package pw.binom

import kotlin.experimental.ExperimentalNativeApi

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalNativeApi::class)
actual class WeakReference<T : Any> actual constructor(value: T) {

  val native = kotlin.native.ref.WeakReference(value)

  actual val get: T?
    get() = native.get()
}
