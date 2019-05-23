package pw.binom.json

interface JsonNode

class JsonObject(val childs: MutableMap<String, JsonNode> = HashMap()) : JsonNode
class JsonArray(val elements: MutableList<JsonNode> = ArrayList()) : JsonNode
class JsonString(val value: String) : JsonNode
class JsonNumber(val value: String) : JsonNode
class JsonBoolean(val value: Boolean) : JsonNode
class JsonNull : JsonNode