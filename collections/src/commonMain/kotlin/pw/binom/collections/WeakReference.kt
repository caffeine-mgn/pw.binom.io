package pw.binom.collections

internal expect class WeakReference<T : Any> {
  constructor(value: T)

  val get: T?
}
