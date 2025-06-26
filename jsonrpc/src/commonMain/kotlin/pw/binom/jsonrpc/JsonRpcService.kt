package pw.binom.jsonrpc

import kotlinx.serialization.KSerializer

interface JsonRpcService {
  interface Method<REQUEST, RESPONSE> {
    val name: String
    val request: KSerializer<REQUEST>
    val response: KSerializer<RESPONSE>
  }

  interface Notification<REQUEST> {
    val name: String
    val request: KSerializer<REQUEST>
  }
}
