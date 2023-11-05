package pw.binom

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class WeakReference<T : Any> actual constructor(value: T) {

  val native = java.lang.ref.WeakReference(value)

  actual val get: T?
    get() = native.get()
}
