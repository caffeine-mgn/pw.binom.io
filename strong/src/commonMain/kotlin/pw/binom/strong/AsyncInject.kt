package pw.binom.strong

interface AsyncInject<T> {
  suspend fun get(): T
}
