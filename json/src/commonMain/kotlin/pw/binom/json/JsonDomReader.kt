package pw.binom.json

class JsonDomReader(private val ready: (JsonNode) -> Unit) : JsonVisiter {

    override suspend fun textValue(value: String) {
        ready(JsonString(value))
    }

    override suspend fun numberValue(value: String) {
        ready(JsonNumber(value))
    }

    override suspend fun nullValue() {
        ready(JsonNull())
    }

    override fun objectValue(): JsonObjectVisiter {
        val r = JsonDomObjectReader()
        ready(r.node)
        return r
    }

    override suspend fun arrayValue(): JsonArrayVisiter {
        val r = JsonDomArrayReader()
        ready(r.node)
        return r
    }

    override suspend fun booleanValue(value: Boolean) {
        ready(JsonBoolean(value))
    }

}

class JsonDomObjectReader : JsonObjectVisiter {

    val node = JsonObject()

    override suspend fun start() {
    }

    override suspend fun property(name: String): JsonVisiter =
            JsonDomReader {
                node.childs[name] = it
            }

    override suspend fun end() {
    }
}

class JsonDomArrayReader : JsonArrayVisiter {
    override suspend fun start() {
    }

    override suspend fun element(): JsonVisiter =
            JsonDomReader {
                node.elements += it
            }

    override suspend fun end() {
    }

    val node = JsonArray()

}