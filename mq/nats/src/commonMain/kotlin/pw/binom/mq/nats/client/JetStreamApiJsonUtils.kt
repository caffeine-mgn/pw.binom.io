package pw.binom.mq.nats.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object JetStreamApiJsonUtils {
  private val json =
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }

  fun <T : Any> encode(
    serializer: KSerializer<T>,
    value: T,
  ) = json.encodeToString(serializer, value).encodeToByteArray()

  inline fun checkError(
    data: ByteArray,
    func: (String) -> Nothing,
  ): JsonElement {
    val j = json.parseToJsonElement(data.decodeToString())
    val asObject = j as? JsonObject
    val error = asObject?.get("error")
    if (error != null) {
      func(error.jsonPrimitive.content)
    }
    return j
  }

  fun <T : Any> decode(
    serializer: KSerializer<T>,
    data: ByteArray,
  ): T {
    val j = json.parseToJsonElement(data.decodeToString())
    val asObject = j as? JsonObject
    if (asObject?.get("error") != null) {
      throw RuntimeException("Has error")
    }
    return json.decodeFromJsonElement(serializer, j)
  }
}
