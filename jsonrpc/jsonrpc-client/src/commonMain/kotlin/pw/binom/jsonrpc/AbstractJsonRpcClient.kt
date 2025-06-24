package pw.binom.jsonrpc

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import pw.binom.atomic.AtomicInt
import pw.binom.jsonrpc.exceptions.*

abstract class AbstractJsonRpcClient {
  protected abstract val json: Json

  protected fun <REQUEST, RESPONSE> bind(method: JsonRpcMethod<REQUEST, RESPONSE>) = bind(
    requestSerializer = method.jsonRpcRequest,
    responseSerializer = method.jsonRpcResponse,
    name = method.jsonRpcMethodName,
  )

  protected fun <REQUEST, RESPONSE> bind(
    requestSerializer: KSerializer<REQUEST>,
    responseSerializer: KSerializer<RESPONSE>,
    name: String = requestSerializer.descriptor.serialName,
  ) = JsonRpcRemoteMethod(
    jsonRpcRequest = requestSerializer,
    jsonRpcResponse = responseSerializer,
    client = this,
    name = name,
  )

  private val idIterator = AtomicInt(0)

  suspend fun <REQUEST, RESPONSE> invocation(
    method: JsonRpcRemoteMethod<REQUEST, RESPONSE>,
    request: REQUEST,
  ): RESPONSE {
    val id = idIterator.increment()
    val req = JsonRpcRequest.Single(
      id = JsonPrimitive(id),
      method = method.name,
      params = request?.let { json.encodeToJsonElement(method.jsonRpcRequest, it) }
    )
    val fullRequest = req.json
    val resp = JsonRpcResponse.parse(invocation(id = id, data = fullRequest))
    val e = when (resp) {
      is JsonRpcResponse.Single -> {
        when (val content = resp.content) {
          is JsonRpcResponse.Content.Error -> throw when (content.code) {
            JsonRpcErrorCodes.PARSE_ERROR -> ParseException(message = content.message, data = content.data)
            JsonRpcErrorCodes.INVALID_REQUEST -> InvalidRequestException(message = content.message, data = content.data)
            JsonRpcErrorCodes.METHOD_NOT_FOUND -> MethodNotFoundException(
              message = content.message,
              data = content.data
            )

            JsonRpcErrorCodes.INVALID_PARAMS -> InvalidParamsException(message = content.message, data = content.data)
            JsonRpcErrorCodes.INTERNAL_ERROR -> InternalErrorException(message = content.message, data = content.data)
            else -> BaseJsonRpcException(code = content.code, message = content.message, data = content.data)
          }

          is JsonRpcResponse.Content.Result -> json.decodeFromJsonElement(
            method.jsonRpcResponse,
            content.body ?: JsonNull
          )
        }
      }

      is JsonRpcResponse.Batch -> throw IllegalStateException("Excepted single response but got batch")
    }
    return e
  }

  protected abstract suspend fun invocation(
    id: Int,
    data: JsonElement,
  ): JsonElement

  @OptIn(InternalSerializationApi::class)
  protected inline fun <reified REQUEST : Any, reified RESPONSE : Any> bind(name: String = REQUEST::class.serializer().descriptor.serialName) =
    bind(
      requestSerializer = REQUEST::class.serializer(),
      responseSerializer = RESPONSE::class.serializer(),
      name = name,
    )
}
