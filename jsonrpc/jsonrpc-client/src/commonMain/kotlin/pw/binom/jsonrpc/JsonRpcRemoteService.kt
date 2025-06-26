package pw.binom.jsonrpc

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import pw.binom.atomic.AtomicInt
import pw.binom.jsonrpc.exceptions.BaseJsonRpcException
import pw.binom.jsonrpc.exceptions.InternalErrorException
import pw.binom.jsonrpc.exceptions.InvalidParamsException
import pw.binom.jsonrpc.exceptions.InvalidRequestException
import pw.binom.jsonrpc.exceptions.MethodNotFoundException
import pw.binom.jsonrpc.exceptions.ParseException
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@OptIn(InternalSerializationApi::class)
abstract class JsonRpcRemoteService {
  protected abstract val invocator: JsonRpcInvocator
  protected abstract val json: Json

  interface RemoteMethod<REQUEST, RESPONSE> : JsonRpcService.Method<REQUEST, RESPONSE> {
    suspend operator fun invoke(request: REQUEST): RESPONSE
  }

  private val idCounter = AtomicInt(0)

  private inner class RemoteMethodImpl<REQUEST, RESPONSE>(
    override val name: String,
    override val request: KSerializer<REQUEST>,
    override val response: KSerializer<RESPONSE>,
  ) : RemoteMethod<REQUEST, RESPONSE> {
    override suspend fun invoke(request: REQUEST): RESPONSE {
      val id = idCounter.increment()
      val rec = JsonRpcRequest.Single(
        id = JsonPrimitive(id),
        method = name,
        params = json.encodeToJsonElement(this.request, request),
      )
      val resp = invocator.invoke(rec)
      check(resp is JsonRpcResponse.Single) { "Excepted single response, but got batch response" }

      return when (val content = resp.content) {
        is JsonRpcResponse.Content.Result -> json.decodeFromJsonElement(this.response, content.body ?: JsonNull)
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
      }
    }
  }

  protected fun <REQUEST, RESPONSE> define(
    name: String,
    request: KSerializer<REQUEST>,
    response: KSerializer<RESPONSE>,
  ): RemoteMethod<REQUEST, RESPONSE> = RemoteMethodImpl(
    name = name,
    request = request,
    response = response,
  )

  protected inline fun <reified REQUEST : Any, reified RESPONSE : Any> define() =
    define(
      request = REQUEST::class.serializer(),
      response = RESPONSE::class.serializer(),
    )

  protected fun <REQUEST, RESPONSE> define(
    request: KSerializer<REQUEST>,
    response: KSerializer<RESPONSE>,
  ) =
    object : PropertyDelegateProvider<Any, ReadOnlyProperty<Any, RemoteMethod<REQUEST, RESPONSE>>> {
      override fun provideDelegate(
        thisRef: Any,
        property: KProperty<*>,
      ) = object : ReadOnlyProperty<Any, RemoteMethod<REQUEST, RESPONSE>> {
        override fun getValue(
          thisRef: Any,
          property: KProperty<*>,
        ): RemoteMethod<REQUEST, RESPONSE> = define(
          name = property.name,
          request = request,
          response = response,
        )

      }
    }
}
