package pw.binom.jsonrpc.server

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import pw.binom.jsonrpc.JSONRPC
import pw.binom.jsonrpc.exceptions.JsonRpcException

abstract class AbstractJsonRpcServer {
  companion object;

  protected abstract fun findMethod(name: String): JsonRpcMethod<*, *>?
  protected abstract val json: Json

  suspend fun income(element: JsonElement): JsonElement =
    when (element) {
      is JsonArray -> JsonArray(element.map {
          singleProcessing(it)
      })

      is JsonObject -> singleProcessing(element)
      else -> genError(JsonRpcException("Invalid root element"))
    }

  suspend fun genError(error: Throwable, id: JsonElement? = null) =
      buildJsonObject {
          put(JSONRPC.VERSION_FIELD, JsonPrimitive(JSONRPC.JSONRPC_V2))
          if (id != null) {
              put(JSONRPC.ID_FIELD, id)
          }
          put(JSONRPC.ERROR_FIELD, errorProcessing(error))
      }

  private suspend fun singleProcessing(element: JsonElement): JsonObject {
    if (element !is JsonObject) {
      return genError(JsonRpcException("Root element is not object"))
    }
    val jRpcVersion =
      element[JSONRPC.VERSION_FIELD] ?: return genError(
        JsonRpcException("field \"${JSONRPC.VERSION_FIELD}\" is missing"),
        id = element[JSONRPC.ID_FIELD]
      )
    if (jRpcVersion !is JsonPrimitive) {
      return genError(JsonRpcException("Field \"${JSONRPC.VERSION_FIELD}\" is invalid"), id = element[JSONRPC.ID_FIELD])
    }
    if (jRpcVersion.content != JSONRPC.JSONRPC_V2) {
      return genError(
        JsonRpcException("Invalid json rpc version: \"${jRpcVersion.content}\""),
        id = element[JSONRPC.ID_FIELD]
      )
    }
    val method = element[JSONRPC.METHOD_FIELD] ?: throw JsonRpcException("Field \"${JSONRPC.METHOD_FIELD}\" is missing")
    if (method !is JsonPrimitive) {
      return genError(JsonRpcException("Field \"${JSONRPC.METHOD_FIELD}\" is invalid"), id = element[JSONRPC.ID_FIELD])
    }

    return try {
        buildJsonObject {
            put(JSONRPC.VERSION_FIELD, kotlinx.serialization.json.JsonPrimitive(JSONRPC.JSONRPC_V2))
            val id = element[JSONRPC.ID_FIELD]
            if (id != null) {
                put(JSONRPC.ID_FIELD, id)
            }
            put("result", execute(method = method.content, params = element[JSONRPC.PARAMS_FIELD]) ?: JsonNull)
        }
    } catch (e: Throwable) {
      genError(e, id = element[JSONRPC.ID_FIELD])
    }
  }

  protected abstract suspend fun errorProcessing(exception: Throwable): JsonObject

  suspend fun execute(method: String, params: JsonElement?): JsonElement? {
    val jsonMethod = findMethod(method) ?: throw JsonRpcException("Method not found")
    jsonMethod as JsonRpcMethod<Any?, Any?>
    val jsonParam = json.decodeFromJsonElement(jsonMethod.jsonRpcRequest, params ?: JsonNull)
    val result = jsonMethod.executeJsonRpc(jsonParam)
    return json.encodeToJsonElement(jsonMethod.jsonRpcResponse, result)
  }
}
