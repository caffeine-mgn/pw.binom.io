package pw.binom.jsonrpc

import kotlin.properties.ReadOnlyProperty

interface JsonCrossService {
  interface CrossMethod<REQUEST,RESPONSE> : ReadOnlyProperty<Any, CrossMethod<REQUEST, RESPONSE>> {
    suspend operator fun invoke(params: REQUEST): RESPONSE
  }
}
