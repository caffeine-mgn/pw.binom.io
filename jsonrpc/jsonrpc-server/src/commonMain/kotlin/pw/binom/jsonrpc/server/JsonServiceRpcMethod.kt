package pw.binom.jsonrpc.server

import pw.binom.jsonrpc.JsonRpcMethod

interface JsonServiceRpcMethod<REQUEST, RESPONSE> : JsonRpcMethod<REQUEST, RESPONSE> {
  companion object {
    fun <REQUEST, RESPONSE> implement(
      method: JsonRpcMethod<REQUEST, RESPONSE>,
      func: suspend (REQUEST) -> RESPONSE,
    ) =
      object : JsonServiceRpcMethod<REQUEST, RESPONSE> by method {
        override suspend fun executeJsonRpc(request: REQUEST): RESPONSE = func(request)
      }
  }

  suspend fun executeJsonRpc(request: REQUEST): RESPONSE
}
