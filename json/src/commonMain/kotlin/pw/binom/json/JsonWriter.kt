package pw.binom.json

import pw.binom.io.AsyncAppendable

class JsonWriter(private val output: AsyncAppendable) : JsonVisiter {
    override suspend fun numberValue(value: String) {
        checkDone()
        output.append(value)
        done = true
    }

    override suspend fun booleanValue(value: Boolean) {
        output.append(if (value) "true" else "false")
    }

    private var done = false

    private fun checkDone() {
        if (done)
            throw IllegalStateException("Json Write already done")
    }

    override suspend fun textValue(value: String) {
        checkDone()
        output.append('\"')
        value.forEach {
            when (it) {
                '\n' -> output.append("\\n")
                '\r' -> output.append("\\r")
                '\t' -> output.append("\\t")
                '"' -> output.append("\\\"")
                '\\' -> output.append("\\\\")
                else -> output.append(it)
            }
        }
        output.append('\"')
        done = true
    }

    override suspend fun nullValue() {
        checkDone()
        output.append("null")
        done = true
    }

    override fun objectValue(): JsonObjectVisiter {
        checkDone()
        done = true
        return JsonObjectWriter(output)
    }

    override suspend fun arrayValue(): JsonArrayVisiter {
        checkDone()
        done = true
        return JsonArrayWriter(output)
    }
}

private class JsonObjectWriter(val output: AsyncAppendable) : JsonObjectVisiter {
    private var first = true
    override suspend fun start() {
        output.append('{')
    }

    override suspend fun property(name: String): JsonVisiter {
        if (!first)
            output.append(',')
        first = false
        output.append('"').append(name).append('"').append(':')
        return JsonWriter(output)
    }

    override suspend fun end() {
        output.append('}')
    }
}

private class JsonArrayWriter(val output: AsyncAppendable) : JsonArrayVisiter {
    private var started = false
    private var done = false
    override suspend fun element(): JsonVisiter {
        if (!started)
            throw IllegalStateException("Json Array Writer not started")
        if (done)
            throw IllegalStateException("Json Array Writer already done")
        if (!first)
            output.append(',')
        first = false
        return JsonWriter(output)
    }

    private var first = true
    override suspend fun start() {
        if (started)
            throw IllegalStateException("Json Array Writer already started")
        if (done)
            throw IllegalStateException("Json Array Writer already done")
        started = true
        output.append('[')
    }

    override suspend fun end() {
        if (!started)
            throw IllegalStateException("Json Array Writer not started")
        if (done)
            throw IllegalStateException("Json Array Writer already done")
        output.append(']')
        done = true
    }
}