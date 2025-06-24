package pw.binom.jsonrpc.server

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import pw.binom.jsonrpc.JSONRPC
import pw.binom.jsonrpc.JsonRpcRequest
import pw.binom.jsonrpc.JsonRpcResponse
import pw.binom.jsonrpc.exceptions.InternalErrorException
import pw.binom.jsonrpc.exceptions.JsonRpcException
import pw.binom.jsonrpc.exceptions.MethodNotFoundException

abstract class AbstractJsonRpcServer {
  companion object;

  protected abstract fun findMethod(name: String): JsonServiceRpcMethod<*, *>?
  protected abstract val json: Json

  suspend fun income(element: JsonElement): JsonElement? = JsonRpcRequest.execute(element) { req ->
    singleProcessing(req)
  }?.json

  private suspend fun singleProcessing(element: JsonRpcRequest.Single): JsonRpcResponse.Single? {
    val content = try {
      execute(method = element.method, params = element.params)
    } catch (e: Throwable) {
      return errorProcessing(id = element.id, exception = e)
    }
    return JsonRpcResponse.Single(
      id = element.id, content = JsonRpcResponse.Content.Result(content)
    )
  }

  protected open suspend fun errorProcessing(id: JsonPrimitive?, exception: Throwable): JsonRpcResponse.Single =
    when (exception) {
      is JsonRpcException -> JsonRpcResponse.Single(
        id = id, content = exception.asResponseContent()
      )

      else -> JsonRpcResponse.Single(
        id = id, content = InternalErrorException(exception.toString()).asResponseContent()
      )
    }

  suspend fun execute(method: String, params: JsonElement?): JsonElement? {
    val jsonMethod = findMethod(method) ?: throw MethodNotFoundException("Method \"$method\" not found")
    jsonMethod as JsonServiceRpcMethod<Any?, Any?>
    val jsonParam = json.decodeFromJsonElement(jsonMethod.jsonRpcRequest, params ?: JsonNull)
    val result = jsonMethod.executeJsonRpc(jsonParam)
    return json.encodeToJsonElement(jsonMethod.jsonRpcResponse, result)
  }
}
