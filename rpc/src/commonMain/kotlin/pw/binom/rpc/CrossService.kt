package pw.binom.rpc

import kotlin.properties.ReadOnlyProperty

interface CrossService {
    val name: String


    interface CrossMethod<REQUEST,RESPONSE> : ReadOnlyProperty<Any, CrossMethod<REQUEST,RESPONSE>> {
        suspend operator fun invoke(params: REQUEST): RESPONSE
    }
}
