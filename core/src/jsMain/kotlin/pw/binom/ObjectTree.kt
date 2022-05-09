package pw.binom

actual class ObjectTree<T>(b: Boolean, val value: T)

actual inline fun <T> ObjectTree(noinline value: () -> T): ObjectTree<T> = ObjectTree(false, value())
actual inline fun <reified T : Any> ObjectTree<T>.attach(): T = value
