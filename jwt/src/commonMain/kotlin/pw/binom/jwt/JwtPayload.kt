package pw.binom.jwt

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

@JvmInline
value class JwtPayload(private val raw: ByteArray) {
  constructor(text: String) : this(text.encodeToByteArray())
  constructor(json: JsonElement) : this(Json.encodeToString(JsonElement.serializer(), json))

  val bytes
    get() = raw
  val text
    get() = bytes.decodeToString()

  val json
    get() = Json.decodeFromString(JsonElement.serializer(), text)
}
