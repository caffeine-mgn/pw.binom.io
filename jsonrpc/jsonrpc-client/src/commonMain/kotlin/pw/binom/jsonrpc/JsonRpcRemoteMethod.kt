package pw.binom.jsonrpc

import kotlinx.serialization.KSerializer

class JsonRpcRemoteMethod<REQUEST, RESPONSE>(
  val jsonRpcRequest: KSerializer<REQUEST>,
  val jsonRpcResponse: KSerializer<RESPONSE>,
  val name: String,
  private val client: AbstractJsonRpcClient,
) {
  suspend operator fun invoke(request: REQUEST): RESPONSE = client.invocation(
    method = this,
    request = request,
  )
}
