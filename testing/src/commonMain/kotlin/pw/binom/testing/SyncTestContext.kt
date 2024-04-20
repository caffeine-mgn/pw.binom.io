package pw.binom.testing

interface SyncTestContext {
  fun init(func: () -> Unit)
  fun dispose(func: () -> Unit)
  fun test(
    name: String,
    ignore: Boolean = false,
    func: () -> Unit,
  )
}
