package pw.binom.jsonrpc

import kotlinx.serialization.KSerializer

interface JsonRpcNotification<REQUEST> {
  companion object {
    fun <REQUEST> create(
      name: String = jsonRpcRequest.descriptor.serialName,
      description: String? = null,
      jsonRpcRequest: KSerializer<REQUEST>,
    ): JsonRpcNotification<REQUEST> =
      object : JsonRpcNotification<REQUEST> {
        override val jsonRpcRequest: KSerializer<REQUEST>
          get() = jsonRpcRequest
        override val jsonRpcMethodName: String
          get() = name
        override val jsonRpcDescription: String?
          get() = description
      }
  }

  val jsonRpcRequest: KSerializer<REQUEST>
  val jsonRpcMethodName
    get() = jsonRpcRequest.descriptor.serialName
  val jsonRpcDescription: String?
    get() = null
}
