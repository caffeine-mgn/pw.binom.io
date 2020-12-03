package pw.binom.rpc

import kotlin.properties.ReadOnlyProperty

interface CrossService {
    val name: String

    interface CrossMethod<T> : ReadOnlyProperty<Any, CrossMethod<T>> {
        suspend operator fun invoke(params: Map<String, Any>): T
    }

}