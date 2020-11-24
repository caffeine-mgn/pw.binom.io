package pw.binom

import kotlin.reflect.KClass

expect class ObjectTree<T : Any> {
    companion object {
        fun <T : Any> create(value: T): ObjectTree<T>
    }
}

expect inline fun <reified T : Any> ObjectTree<T>.attach():T