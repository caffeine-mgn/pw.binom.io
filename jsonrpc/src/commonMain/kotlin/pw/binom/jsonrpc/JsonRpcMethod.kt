package pw.binom.jsonrpc

import kotlinx.serialization.KSerializer

interface JsonRpcMethod<REQUEST, RESPONSE> {
  companion object {
    fun <REQUEST, RESPONSE> create(
      name: String = jsonRpcRequest.descriptor.serialName,
      description: String? = null,
      jsonRpcRequest: KSerializer<REQUEST>,
      jsonRpcResponse: KSerializer<RESPONSE>,
    ): JsonRpcMethod<REQUEST, RESPONSE> =
      object : JsonRpcMethod<REQUEST, RESPONSE> {
        override val jsonRpcRequest: KSerializer<REQUEST>
          get() = jsonRpcRequest
        override val jsonRpcResponse: KSerializer<RESPONSE>
          get() = jsonRpcResponse
        override val jsonRpcMethodName: String
          get() = name
        override val jsonRpcDescription: String?
          get() = description
      }
  }

  val jsonRpcRequest: KSerializer<REQUEST>
  val jsonRpcResponse: KSerializer<RESPONSE>
  val jsonRpcMethodName
    get() = jsonRpcRequest.descriptor.serialName
  val jsonRpcDescription: String?
    get() = null
}
