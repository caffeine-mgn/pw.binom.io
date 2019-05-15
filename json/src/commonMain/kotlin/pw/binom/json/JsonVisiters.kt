package pw.binom.json

interface JsonVisiter {
    fun textValue(value: String)
    fun numberValue(value: Double)
    fun nullValue()
    fun objectValue(): JsonObjectVisiter
    fun arrayValue(): JsonArrayVisiter
    fun booleanValue(value: Boolean)
}

interface JsonObjectVisiter {
    fun start()
    fun property(name: String): JsonVisiter
    fun end()
}

interface JsonArrayVisiter {
    fun start()
    fun element(): JsonVisiter
    fun end()
}

