package pw.binom.json

import pw.binom.io.*


class JsonReader(reader: AsyncReader) {
    val reader = ComposeAsyncReader().addLast(reader)

    suspend fun accept(visiter: JsonVisiter) {
        when (val v = reader.readNoSpace()?.toInt()) {
            '{'.toInt() -> {
                parseObject(visiter.objectValue());return
            }
            '"'.toInt() -> {
                visiter.textValue(parseString());return
            }
            '['.toInt() -> {
                parseArray(visiter.arrayValue());return
            }
            null -> throw EOFException()
            else -> reader.addFirst(v.toChar())
        }

        when (val v = reader.word()) {
            "false" -> visiter.booleanValue(false)
            "true" -> visiter.booleanValue(true)
            "null" -> visiter.nullValue()
            else -> {
                if (v.isNumber)
                    visiter.numberValue(v)
                else
                    throw JsonSaxException("Unknown word \"$v\"")
            }
        }
    }

    private suspend fun parseArray(arrayVisiter: JsonArrayVisiter) {
        arrayVisiter.start()
        while (true) {
            when (val c = reader.readNoSpace()?.toInt()) {
                ']'.toInt() -> {
                    arrayVisiter.end()
                    return
                }
                ','.toInt() -> {
                }
                null -> throw EOFException()
                else -> {
                    reader.addFirst(c.toChar().toString().asReader().asAsync())
                    accept(arrayVisiter.element())
                }
            }
        }
    }

    private suspend fun parseObject(objectVisiter: JsonObjectVisiter) {
        objectVisiter.start()
        while (true) {
            when (val c = reader.readNoSpace()?.toInt()) {
                '}'.toInt() -> {
                    objectVisiter.end()
                    return
                }
                '"'.toInt() -> {
                    parseProperty(objectVisiter)
                }
                ','.toInt() -> {
                }
                null -> throw EOFException()
                else -> JsonSaxException("Unknown char \"$c\"")
            }
        }
    }

    private suspend fun parseProperty(objectVisiter: JsonObjectVisiter) {
        val name = parseString()
        if (reader.readNoSpace() != ':')
            throw ExpectedException(":")
        accept(objectVisiter.property(name))
    }

    private suspend fun parseString(): String {
        val sb = StringBuilder()
        while (true) {
            var c = reader.readChar()?.toInt()
            if (c == '"'.toInt())
                break
            if (c == '\\'.toInt()) {
                c = reader.readChar()?.toInt()
                c = when (c) {
                    'n'.toInt() -> '\n'.toInt()
                    'r'.toInt() -> '\r'.toInt()
                    't'.toInt() -> '\t'.toInt()
                    else -> c
                }
            }
            c ?: throw EOFException()
            sb.append(c.toChar())
        }
        return sb.toString()
    }
}

private suspend fun ComposeAsyncReader.readNoSpace(): Char? {
    while (true) {
        try {
            val c = readChar()?.toInt()
            if (c != '\r'.toInt() && c != '\n'.toInt() && c != ' '.toInt())
                return c?.toChar()
        } catch (e: EOFException) {
            return null
        }
    }
}

private suspend fun ComposeAsyncReader.skipSpaces() {
    while (true) {
        try {
            val c = readChar()?.toInt()
            if (c != ' '.toInt() && c != '\n'.toInt() && c != '\r'.toInt()) {
                addFirst(c?.toChar()?.toString()?.asReader()?.asAsync()?:break)
                break
            }
        } catch (e: EOFException) {
            break
        }
    }
}

private fun ComposeAsyncReader.addFirst(char: Char): ComposeAsyncReader = addFirst(char.toString())
private fun ComposeAsyncReader.addFirst(text: String): ComposeAsyncReader = addFirst(text.asReader().asAsync())

private suspend fun ComposeAsyncReader.word(): String {
    skipSpaces()
    val sb = StringBuilder()
    while (true) {
        try {
            val c = readChar() ?: return sb.toString()
            if (c.isBreak && c.toInt() != '.'.toInt()) {
                addFirst(c)
                return sb.toString()
            }
            sb.append(c)
        } catch (e: EOFException) {
            return sb.toString()
        }
    }

}

private val Char.isNumber: Boolean
    get() = toInt() in 48..57

private val Char.isBreak: Boolean
    get() = toInt() in 0..47 || toInt() in 58..63 || toInt() in 91..96 || toInt() in 123..127

private val String.isNumber: Boolean
    get() {
        var b = false
        forEach {
            if (it == '.') {
                if (b)
                    return false
                b = true
                return@forEach
            }
            if (!it.isNumber)
                return false
        }
        return true
    }