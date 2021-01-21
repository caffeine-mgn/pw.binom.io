package pw.binom

import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.attach

actual inline fun <reified T : Any> ObjectTree<T>.attach() =
    this.attach()

actual inline fun <T> ObjectTree(noinline value: ()->T): ObjectTree<T> =
    DetachedObjectGraph(TransferMode.SAFE, value)

actual typealias ObjectTree<T> = DetachedObjectGraph<T>