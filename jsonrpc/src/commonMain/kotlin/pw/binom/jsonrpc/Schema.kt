package pw.binom.jsonrpc

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import pw.binom.jsonrpc.annotations.Description

object Schema {
  private val NULL = JsonPrimitive("null")
  fun common(e: SerialDescriptor): JsonObject {
    return when (e.kind) {
      StructureKind.OBJECT,
      StructureKind.CLASS,
        -> obj(e)

      PolymorphicKind.OPEN -> TODO()
      PolymorphicKind.SEALED -> TODO()
      PrimitiveKind.BOOLEAN -> boolean(e)
      PrimitiveKind.SHORT,
      PrimitiveKind.LONG,
      PrimitiveKind.INT,
      PrimitiveKind.FLOAT,
      PrimitiveKind.DOUBLE,
      PrimitiveKind.BYTE,
        -> number(e)

      PrimitiveKind.CHAR -> TODO()
      PrimitiveKind.STRING -> str(e)
      SerialKind.CONTEXTUAL -> TODO()
      SerialKind.ENUM -> enum(e)
      StructureKind.LIST -> list(e)
      StructureKind.MAP -> TODO()
    }
  }

  private fun list(e: SerialDescriptor) = buildJsonObject {
    put("type", e.type("array"))
    put("items", common(e.getElementDescriptor(0)))
  }

  private fun boolean(e: SerialDescriptor) = buildJsonObject {
    put("type", e.type("boolean"))
  }

  private fun SerialDescriptor.type(name: String) =
    if (isNullable) {
      JsonArray(listOf(JsonPrimitive(name), NULL))
    } else {
      JsonPrimitive(name)
    }

  private fun number(e: SerialDescriptor) = buildJsonObject {
    put("type", e.type("number"))
  }

  private fun enum(e: SerialDescriptor) = buildJsonObject {
    put("type", e.type("string"))
    put("enum", JsonArray(e.elementNames.map { JsonPrimitive(it) }))
  }

  private fun str(e: SerialDescriptor) = buildJsonObject {
    put("type", e.type("string"))
  }

  private fun obj(e: SerialDescriptor): JsonObject {
    val description = e.getAnnotation<Description>()?.description

    val result = HashMap<String, JsonElement>()
    result["type"] = JsonPrimitive("object")
    if (description != null) {
      result["description"] = JsonPrimitive(description)
    }
    val properties = HashMap<String, JsonElement>()
    repeat(e.elementsCount) { index ->
      val desc = e.getElementAnnotation<Description>(index)?.description
      var obj = common(e.getElementDescriptor(index))
      if (desc != null) {
        val ee = HashMap(obj)
        ee["description"] = JsonPrimitive(desc)
        obj = JsonObject(ee)
      }
      properties[e.getElementName(index)] = obj
    }
    result["properties"] = JsonObject(properties)
    val required = ArrayList<JsonPrimitive>()
    repeat(e.elementsCount) { index ->
      if (!e.isElementOptional(index)) {
        required += JsonPrimitive(e.getElementName(index))
      }
    }
    result["required"] = JsonArray(required)
    result["additionalProperties"] = JsonPrimitive(false)
    return JsonObject(result)
  }

  private inline fun <reified T : Any> SerialDescriptor.getElementAnnotation(index: Int): T? =
    getElementAnnotations(index).find { it is T } as T?

  private inline fun <reified T : Any> SerialDescriptor.getAnnotation(): T? =
    annotations.find { it is T } as T?
}
