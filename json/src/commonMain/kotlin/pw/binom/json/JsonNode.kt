package pw.binom.json

interface JsonNode

class JsonObject(private val childs: MutableMap<String, JsonNode> = HashMap()) : JsonNode, MutableMap<String, JsonNode> by childs

class JsonArray(private val elements: MutableList<JsonNode> = ArrayList()) : JsonNode, MutableList<JsonNode> by elements
class JsonString(val value: String) : JsonNode
class JsonNumber(val value: String) : JsonNode
class JsonBoolean(val value: Boolean) : JsonNode
class JsonNull : JsonNode

val JsonNode.text: String
    get() = when (this) {
        is JsonString -> this.value
        is JsonNumber -> this.value
        is JsonBoolean -> if (this.value) "true" else "false"
        is JsonNull -> "null"
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.long: Long
    get() = when (this) {
        is JsonString -> this.value.toLongOrNull() ?: throw RuntimeException("Can't parse \"$value\" to long")
        is JsonNumber -> this.value.toLongOrNull() ?: throw RuntimeException("Can't parse \"$value\" to long")
        is JsonBoolean -> if (this.value) 1 else 0
        is JsonNull -> 0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.int: Int
    get() = when (this) {
        is JsonString -> this.value.toIntOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonNumber -> this.value.toIntOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonBoolean -> if (this.value) 1 else 0
        is JsonNull -> 0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.boolean: Boolean
    get() = when (this) {
        is JsonString -> this.value == "true"
        is JsonNumber -> (this.value.toDoubleOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")) > 0
        is JsonBoolean -> this.value
        is JsonNull -> false
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.float: Float
    get() = when (this) {
        is JsonString -> this.value.toFloatOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonNumber -> this.value.toFloatOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonBoolean -> if (this.value) 1f else 0f
        is JsonNull -> 0f
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.double: Double
    get() = when (this) {
        is JsonString -> this.value.toDoubleOrNull() ?: throw RuntimeException("Can't parse \"$value\" to double")
        is JsonNumber -> this.value.toDoubleOrNull() ?: throw RuntimeException("Can't parse \"$value\" to double")
        is JsonBoolean -> if (this.value) 1.0 else 0.0
        is JsonNull -> 0.0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.isNull: Boolean
    get() = this is JsonNull

val JsonNode.array: JsonArray
    get() {
        if (this !is JsonArray)
            throw RuntimeException("Node $this is not array")
        return this
    }

val JsonNode.obj: JsonObject
    get() {
        if (this !is JsonObject)
            throw RuntimeException("Node $this is not object")
        return this
    }