package pw.binom.jsonrpc.server

import kotlinx.serialization.KSerializer

interface JsonRpcMethod<REQUEST, RESPONSE> {
  val jsonRpcRequest: KSerializer<REQUEST>
  val jsonRpcResponse: KSerializer<RESPONSE>
  val jsonRpcMethodName
    get() = jsonRpcRequest.descriptor.serialName
  val jsonRpcDescription: String?
    get() = null

  suspend fun executeJsonRpc(request: REQUEST): RESPONSE
}
