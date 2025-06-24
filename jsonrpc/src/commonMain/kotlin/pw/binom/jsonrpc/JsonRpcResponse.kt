package pw.binom.jsonrpc

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import pw.binom.jsonrpc.exceptions.ParseException

sealed interface JsonRpcResponse {
  companion object {
    fun parse(json: JsonElement) =
      when (json) {
        is JsonArray -> {
          val responseList = json.mapNotNull { jsonElement ->
            if (jsonElement !is JsonObject) {
              throw ParseException("Unexpected root object type")
            } else {
              Single.parse(jsonElement)
            }
          }
          Batch(responseList)
        }

        is JsonObject -> Single.parse(json)
        else -> throw ParseException("Invalid root object ${json::class.simpleName}")
      }
  }

  val json: JsonElement

  data class Single(
    val id: JsonPrimitive?,
    val content: Content,
  ) : JsonRpcResponse {

    companion object {
      fun parse(json: JsonObject): Single {
        val id = json[JSONRPC.ID_FIELD]
        if (id != null && id !is JsonPrimitive) {
          throw ParseException("Invalid field \"${JSONRPC.ID_FIELD}\" type. Should be number or string")
        }
        val result = json[JSONRPC.RESULT_FIELD]
        val error = json[JSONRPC.ERROR_FIELD]
        val content = when {
          result != null && error != null -> throw ParseException("Response should only one of field \"${JSONRPC.RESULT_FIELD}\" or \"${JSONRPC.ERROR_FIELD}\"")
          error != null && error !is JsonObject -> throw ParseException("Invalid field \"${JSONRPC.ERROR_FIELD}\" type. Should be object")
          error != null -> Content.Error.parse(error)
          result == null || result == JsonNull -> Content.Result.EMPTY
          else -> Content.Result(result)
        }
        return Single(
          id = id,
          content = content,
        )
      }
    }

    override val json: JsonElement
      get() = buildJsonObject {
        put(JSONRPC.VERSION_FIELD, JsonPrimitive(JSONRPC.JSONRPC_V2))
        if (id != null) {
          put(JSONRPC.ID_FIELD, id)
        }
        when (content) {
          is Content.Error -> put(JSONRPC.ERROR_FIELD, content.json)
          is Content.Result -> if (content.body != null) {
            put(JSONRPC.RESULT_FIELD, content.body)
          }
        }
      }
  }

  sealed interface Content {
    data class Result(val body: JsonElement?) : Content {
      companion object {
        val EMPTY = Result(null)
      }
    }

    data class Error(val message: String, val code: Int, val data: JsonElement?) : Content {
      companion object {
        fun parse(json: JsonObject): Error {
          val code = json[JSONRPC.ERROR_CODE_FIELD]
            ?: throw ParseException("Missing error field \"${JSONRPC.ERROR_CODE_FIELD}\"")
          val message = json[JSONRPC.ERROR_MESSAGE_FIELD]
            ?: throw ParseException("Missing error field \"${JSONRPC.ERROR_MESSAGE_FIELD}\"")

          if (code !is JsonPrimitive) {
            throw ParseException("Error field \"${JSONRPC.ERROR_CODE_FIELD}\" should be Primitive")
          }
          if (code.intOrNull == null) {
            throw ParseException("Error field \"${JSONRPC.ERROR_CODE_FIELD}\" should be Integer")
          }
          if (message !is JsonPrimitive) {
            throw ParseException("Error field \"${JSONRPC.ERROR_MESSAGE_FIELD}\" should be Primitive")
          }
          return Error(
            code = code.int,
            message = message.content,
            data = json[JSONRPC.ERROR_DATA_FIELD],
          )
        }
      }

      val json = buildJsonObject {
        put(JSONRPC.ERROR_CODE_FIELD, JsonPrimitive(code))
        put(JSONRPC.ERROR_MESSAGE_FIELD, JsonPrimitive(message))
        if (data != null) {
          put(JSONRPC.ERROR_DATA_FIELD, data)
        }
      }
    }
  }

  data class Batch(val responses: List<Single>) : JsonRpcResponse {
    override val json: JsonElement
      get() = buildJsonArray {
        responses.forEach { response ->
          add(response.json)
        }
      }
  }
}
