package pw.binom

import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.native.concurrent.attach

actual class ObjectTree<T : Any>(val value: DetachedObjectGraph<T>) {
    actual companion object {
        actual fun <T : Any> create(value: T): ObjectTree<T> =
            ObjectTree(DetachedObjectGraph {
                value
            })
    }
}

actual inline fun <reified T : Any> ObjectTree<T>.attach() =
    value.attach()