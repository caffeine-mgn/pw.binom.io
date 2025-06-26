package pw.binom.jsonrpc

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import pw.binom.jsonrpc.exceptions.InternalErrorException
import pw.binom.jsonrpc.exceptions.InvalidRequestException
import pw.binom.jsonrpc.exceptions.JsonRpcException
import pw.binom.jsonrpc.exceptions.ParseException

sealed interface JsonRpcRequest {
  companion object {
    suspend fun execute(json: JsonElement, func: suspend (Single) -> JsonRpcResponse.Single?) =
      when (json) {
        is JsonArray -> {
          val responseList = json.mapNotNull { jsonElement ->
            if (jsonElement !is JsonObject) {
              JsonRpcResponse.Single(
                id = null,
                content = ParseException("Unexpected root object type").asResponseContent()
              )
            } else {
              Single.execute(jsonElement, func)
            }
          }
          JsonRpcResponse.Batch(responseList)
        }

        is JsonObject -> Single.execute(json, func)

        else -> JsonRpcResponse.Single(
          id = null,
          content = ParseException("Unexpected root object type").asResponseContent()
        )
      }
  }

  suspend fun execute(func: suspend (Single) -> JsonRpcResponse.Single?): JsonRpcResponse?

  val json: JsonElement

  data class Single(
    val id: JsonPrimitive?,
    val method: String,
    val params: JsonElement?,
  ) : JsonRpcRequest {
    companion object {
      fun parse(json: JsonObject): Single {
        val id = json[JSONRPC.ID_FIELD]?.takeIf { it != JsonNull }
        if (id != null && id !is JsonPrimitive) {
          throw InvalidRequestException("Field \"${JSONRPC.ID_FIELD}\" should be string or number")
        }
        val method = json[JSONRPC.METHOD_FIELD]?.jsonPrimitive?.content
          ?: throw InvalidRequestException("Missing field \"${JSONRPC.METHOD_FIELD}\"")
        val params = json[JSONRPC.PARAMS_FIELD]
        return Single(
          id = id,
          method = method,
          params = params,
        )
      }

      suspend fun execute(json: JsonObject, func: suspend (Single) -> JsonRpcResponse.Single?) =
        parse(json).execute(func)
    }

    override suspend fun execute(func: suspend (Single) -> JsonRpcResponse.Single?) =
      try {
        func(this)
      } catch (e: JsonRpcException) {
        val id = json[JSONRPC.ID_FIELD]?.let { it as? JsonPrimitive }
        JsonRpcResponse.Single(
          id = id,
          content = e.asResponseContent()
        )
      } catch (e: Throwable) {
        val id = json[JSONRPC.ID_FIELD]?.let { it as? JsonPrimitive }
        JsonRpcResponse.Single(
          id = id,
          content = InternalErrorException(e.toString()).asResponseContent()
        )
      }

    override val json
      get() = buildJsonObject {
        put(JSONRPC.VERSION_FIELD, JsonPrimitive(JSONRPC.JSONRPC_V2))
        if (id != null) {
          put(JSONRPC.ID_FIELD, id)
        }
        put(JSONRPC.METHOD_FIELD, JsonPrimitive(method))
        if (params != null) {
          put(JSONRPC.PARAMS_FIELD, params)
        }
      }
  }

  data class Batch(val requests: List<JsonRpcRequest.Single>) : JsonRpcRequest {
    companion object {

    }

    override suspend fun execute(func: suspend (Single) -> JsonRpcResponse.Single?): JsonRpcResponse.Batch? {
      val resp = ArrayList<JsonRpcResponse.Single>(requests.size)
      requests.forEach {
        resp += it.execute(func) ?: return@forEach
      }
      return if (resp.isNotEmpty()) {
        JsonRpcResponse.Batch(resp)
      } else {
        null
      }
    }

    override val json: JsonElement
      get() = buildJsonArray {
        requests.forEach { request ->
          add(request.json)
        }
      }
  }
}
