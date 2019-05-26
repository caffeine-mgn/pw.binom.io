package pw.binom.json

import pw.binom.io.AsyncReader

class JsonDomReader() : JsonVisiter {
    private var ready: ((JsonNode) -> Unit)? = null

    private var _node: JsonNode? = null

    val node: JsonNode
        get() = _node ?: throw IllegalStateException("Node not readed")

    constructor(ready: (JsonNode) -> Unit) : this() {
        this.ready = ready
    }

    private fun done(node: JsonNode) {
        ready?.invoke(node)
        _node = node
    }

    override suspend fun textValue(value: String) {
        done(JsonString(value))
    }

    override suspend fun numberValue(value: String) {
        done(JsonNumber(value))
    }

    override suspend fun nullValue() {
        done(JsonNull())
    }

    override fun objectValue(): JsonObjectVisiter {
        val r = JsonDomObjectReader()
        done(r.node)
        return r
    }

    override suspend fun arrayValue(): JsonArrayVisiter {
        val r = JsonDomArrayReader()
        done(r.node)
        return r
    }

    override suspend fun booleanValue(value: Boolean) {
        done(JsonBoolean(value))
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

suspend fun AsyncReader.parseJSON(): JsonNode {
    val r = JsonDomReader()
    JsonReader(this).accept(r)
    return r.node
}