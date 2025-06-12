package pw.binom.jsonrpc

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

abstract class AbstractJsonRpcClient {
  protected fun <REQUEST, RESPONSE> define(
    requestSerializer: KSerializer<REQUEST>,
    responseSerializer: KSerializer<RESPONSE>,
    name: String = requestSerializer.descriptor.serialName,
  ) = JsonRpcRemoteMethod(
    requestSerializer = requestSerializer,
    responseSerializer = responseSerializer,
    client = this,
    name = name,
  )

  internal abstract suspend fun <REQUEST, RESPONSE> remoteInvocation(
    method: JsonRpcRemoteMethod<REQUEST, RESPONSE>,
    request: REQUEST,
  ): RESPONSE

  @OptIn(InternalSerializationApi::class)
  protected inline fun <reified REQUEST : Any, reified RESPONSE : Any> define(name: String = REQUEST::class.serializer().descriptor.serialName) =
    define(
      requestSerializer = REQUEST::class.serializer(),
      responseSerializer = RESPONSE::class.serializer()
    )
}
