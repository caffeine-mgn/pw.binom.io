package pw.binom.testing

interface AsyncTestContext {
  fun init(func: suspend () -> Unit)
  fun dispose(func: suspend () -> Unit)
  suspend fun test(
    name: String,
    ignore: Boolean = false,
    func: suspend () -> Unit,
  )
}
