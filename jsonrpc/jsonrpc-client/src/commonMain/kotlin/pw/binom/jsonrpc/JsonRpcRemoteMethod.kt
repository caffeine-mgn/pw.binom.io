package pw.binom.jsonrpc

import kotlinx.serialization.KSerializer

class JsonRpcRemoteMethod<REQUEST, RESPONSE>(
  val requestSerializer: KSerializer<REQUEST>,
  val responseSerializer: KSerializer<RESPONSE>,
  val name:String,
  private val client: AbstractJsonRpcClient,
) {
  suspend operator fun invoke(request: REQUEST): RESPONSE = client.remoteInvocation(
    method = this,
    request = request,
  )
}
