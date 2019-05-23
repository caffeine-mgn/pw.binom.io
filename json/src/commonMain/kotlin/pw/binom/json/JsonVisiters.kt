package pw.binom.json

interface JsonVisiter {
    suspend fun textValue(value: String)
    suspend fun numberValue(value: String)
    suspend fun nullValue()
    fun objectValue(): JsonObjectVisiter
    suspend fun arrayValue(): JsonArrayVisiter
    suspend fun booleanValue(value: Boolean)
}

interface JsonObjectVisiter {
    suspend fun start()
    suspend fun property(name: String): JsonVisiter
    suspend fun end()
}

interface JsonArrayVisiter {
    suspend fun start()
    suspend fun element(): JsonVisiter
    suspend fun end()
}

