package pw.binom.json

class JsonWriter(private val output: Appendable) : JsonVisiter {
    override fun booleanValue(value: Boolean) {
        output.append(if (value) "true" else "false")
    }

    private var done = false

    private fun checkDone() {
        if (done)
            throw IllegalStateException("Json Write already done")
    }

    override fun textValue(value: String) {
        checkDone()
        output.append('\"').append(value).append('\"')
        done = true
    }

    override fun numberValue(value: Double) {
        checkDone()
        output.append(value.toString())
        done = true
    }


    override fun nullValue() {
        checkDone()
        output.append("null")
        done = true
    }

    override fun objectValue(): JsonObjectVisiter {
        checkDone()
        done = true
        return JsonObjectWriter(output)
    }

    override fun arrayValue(): JsonArrayVisiter {
        checkDone()
        done = true
        return JsonArrayWriter(output)
    }
}

private class JsonObjectWriter(val output: Appendable) : JsonObjectVisiter {
    private var first = true
    override fun start() {
        output.append('{')
    }

    override fun property(name: String): JsonVisiter {
        if (!first)
            output.append(',')
        first = false
        output.append('"').append(name).append('"').append(':')
        return JsonWriter(output)
    }

    override fun end() {
        output.append('}')
    }
}

private class JsonArrayWriter(val output: Appendable) : JsonArrayVisiter {
    private var started = false
    private var done = false
    override fun element(): JsonVisiter {
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
    override fun start() {
        if (started)
            throw IllegalStateException("Json Array Writer already started")
        if (done)
            throw IllegalStateException("Json Array Writer already done")
        started = true
        output.append('[')
    }

    override fun end() {
        if (!started)
            throw IllegalStateException("Json Array Writer not started")
        if (done)
            throw IllegalStateException("Json Array Writer already done")
        output.append(']')
        done = true
    }
}