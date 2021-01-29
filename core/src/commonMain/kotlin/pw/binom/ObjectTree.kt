package pw.binom

import kotlin.reflect.KClass

expect class ObjectTree<T>

expect inline fun <T> ObjectTree(noinline value: ()->T): ObjectTree<T>
expect inline fun <reified T : Any> ObjectTree<T>.attach(): T