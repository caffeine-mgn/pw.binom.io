package pw.binom

actual class ObjectTree<T : Any>(val value: T) {
    actual companion object {
        actual fun <T : Any> create(value: T): ObjectTree<T> =
            ObjectTree(value)
    }
}

actual inline fun <reified T : Any> ObjectTree<T>.attach() =
    value