package pw.binom.json

import pw.binom.io.AsyncAppendable

interface JsonNode {
    suspend fun accept(visiter: JsonVisiter)
}

class JsonObject(private val childs: MutableMap<String, JsonNode?> = HashMap()) : JsonNode, MutableMap<String, JsonNode?> by childs {
    override suspend fun accept(visiter: JsonVisiter) {
        accept(visiter.objectValue())
    }

    suspend fun accept(visiter: JsonObjectVisiter) {
        visiter.start()
        childs.forEach {
            if (it.value == null)
                visiter.property(it.key).nullValue()
            else
                it.value!!.accept(visiter.property(it.key))
        }
        visiter.end()
    }
}

class JsonArray(private val elements: MutableList<JsonNode?> = ArrayList()) : JsonNode, MutableList<JsonNode?> by elements {
    override suspend fun accept(visiter: JsonVisiter) {
        accept(visiter.arrayValue())
    }

    suspend fun accept(visiter: JsonArrayVisiter) {
        visiter.start()
        forEach {
            if (it == null)
                visiter.element().nullValue()
            else
                it.accept(visiter.element())
        }
        visiter.end()
    }

}

class JsonString(val value: String) : JsonNode {
    override suspend fun accept(visiter: JsonVisiter) {
        visiter.textValue(value)
    }

}

class JsonNumber(val value: String) : JsonNode {
    override suspend fun accept(visiter: JsonVisiter) {
        visiter.numberValue(value)
    }

}

class JsonBoolean(val value: Boolean) : JsonNode {
    override suspend fun accept(visiter: JsonVisiter) {
        visiter.booleanValue(value)
    }

}

val JsonNode.string: String
    get() = when (this) {
        is JsonString -> this.value
        is JsonNumber -> this.value
        is JsonBoolean -> if (this.value) "true" else "false"
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.long: Long
    get() = when (this) {
        is JsonString -> this.value.toLongOrNull() ?: throw RuntimeException("Can't parse \"$value\" to long")
        is JsonNumber -> this.value.toLongOrNull() ?: throw RuntimeException("Can't parse \"$value\" to long")
        is JsonBoolean -> if (this.value) 1 else 0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.int: Int
    get() = when (this) {
        is JsonString -> this.value.toIntOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonNumber -> this.value.toIntOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonBoolean -> if (this.value) 1 else 0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.byte: Byte
    get() = when (this) {
        is JsonString -> this.value.toByteOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonNumber -> this.value.toByteOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonBoolean -> if (this.value) 1 else 0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.boolean: Boolean
    get() = when (this) {
        is JsonString -> this.value == "true"
        is JsonNumber -> (this.value.toDoubleOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")) > 0
        is JsonBoolean -> this.value
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.float: Float
    get() = when (this) {
        is JsonString -> this.value.toFloatOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonNumber -> this.value.toFloatOrNull() ?: throw RuntimeException("Can't parse \"$value\" to float")
        is JsonBoolean -> if (this.value) 1f else 0f
        else -> throw RuntimeException("Can't get string value from node $this")
    }

val JsonNode.double: Double
    get() = when (this) {
        is JsonString -> this.value.toDoubleOrNull() ?: throw RuntimeException("Can't parse \"$value\" to double")
        is JsonNumber -> this.value.toDoubleOrNull() ?: throw RuntimeException("Can't parse \"$value\" to double")
        is JsonBoolean -> if (this.value) 1.0 else 0.0
        else -> throw RuntimeException("Can't get string value from node $this")
    }

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

suspend fun JsonNode.write(out: AsyncAppendable) {
    accept(JsonWriter(out))
}

val Byte.json
    get() = JsonNumber(toString())