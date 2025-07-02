package pw.binom.jsonrpc.server

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer
import pw.binom.jsonrpc.JsonRpcRequest
import pw.binom.jsonrpc.JsonRpcResponse
import pw.binom.jsonrpc.JsonRpcService
import pw.binom.jsonrpc.exceptions.MethodNotFoundException
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
abstract class JsonRpcLocalService {
  companion object;

  class ServerMethod<REQUEST, RESPONSE>(
    override val name: String,
    override val request: KSerializer<REQUEST>,
    override val response: KSerializer<RESPONSE>,
    val implementation: suspend (REQUEST) -> RESPONSE,
  ) : JsonRpcService.Method<REQUEST, RESPONSE>

  private val internalMethods = HashMap<String, ServerMethod<*, *>>()
  val methods: Map<String, ServerMethod<*, *>>
    get() = internalMethods
  protected abstract val json: Json

  @OptIn(InternalSerializationApi::class)
  protected inline fun <reified REQUEST : Any, reified RESPONSE : Any> implement(
    noinline implementation: suspend (REQUEST) -> RESPONSE,
  ) = implement(
    request = REQUEST::class.serializer(),
    response = RESPONSE::class.serializer(),
    implementation = implementation
  )

  protected fun <REQUEST, RESPONSE> implement(
    request: KSerializer<REQUEST>,
    response: KSerializer<RESPONSE>,
    implementation: suspend (REQUEST) -> RESPONSE,
  ) = object : PropertyDelegateProvider<Any, ReadOnlyProperty<Any,ServerMethod<REQUEST, RESPONSE>>> {
    override fun provideDelegate(
      thisRef: Any,
      property: KProperty<*>,
    ) = object :ReadOnlyProperty<Any, ServerMethod<REQUEST, RESPONSE>> {
      override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
      ): ServerMethod<REQUEST, RESPONSE> = implement(
        name = property.name,
        request = request,
        response = response,
        implementation = implementation,
      )
    }
  }

  protected fun <REQUEST, RESPONSE> implement(
    name: String,
    request: KSerializer<REQUEST>,
    response: KSerializer<RESPONSE>,
    implementation: suspend (REQUEST) -> RESPONSE,
  ): ServerMethod<REQUEST, RESPONSE> {
    check(!internalMethods.containsKey(name)) { "Method \"$name\" already exists." }
    val method = ServerMethod(
      name = name,
      request = request,
      response = response,
      implementation = implementation,
    )
    internalMethods[name] = method
    return method
  }

  suspend fun invoke(params: JsonRpcRequest): JsonRpcResponse? =
    params.execute { req ->
      val method =
        internalMethods[req.method] ?: throw MethodNotFoundException(message = "Method ${req.method} not found")
      method as ServerMethod<Any?, Any?>
      val request = json.decodeFromJsonElement(method.request, req.params ?: JsonNull)
      val response = method.implementation(request)
      val responseJson = json.encodeToJsonElement(method.response, response)
      JsonRpcResponse.Single(id = req.id, content = JsonRpcResponse.Content.Result(responseJson))
    }


  suspend fun invoke(params: JsonElement): JsonElement? =
    JsonRpcRequest.Companion.execute(params) { single ->
      invoke(single)
      val method =
        internalMethods[single.method] ?: throw MethodNotFoundException(message = "Method ${single.method} not found")
      method as ServerMethod<Any?, Any?>
      val request = json.decodeFromJsonElement(method.request, single.params ?: JsonNull)
      val response = method.implementation(request)
      val responseJson = json.encodeToJsonElement(method.response, response)
      JsonRpcResponse.Single(id = single.id, content = JsonRpcResponse.Content.Result(responseJson))
    }?.json
}
