package pw.binom

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class WeakReference<T : Any> {
  constructor(value: T)

  val get: T?
}
