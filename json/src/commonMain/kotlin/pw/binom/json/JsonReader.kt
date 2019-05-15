package pw.binom.json

import pw.binom.io.ComposeReader
import pw.binom.io.EOFException
import pw.binom.io.Reader
import pw.binom.io.asReader


class JsonReader(reader: Reader) {
    val reader = ComposeReader().addLast(reader)

    fun accept(visiter: JsonVisiter) {
        when (val v = reader.readNoSpace()) {
            '{' -> {
                parseObject(visiter.objectValue());return
            }
            '"' -> {
                visiter.textValue(parseString());return
            }
            '[' -> {
                parseArray(visiter.arrayValue());return
            }
            null -> throw EOFException()
            else -> reader.addFirst(v)
        }

        when (val v = reader.word()) {
            "false" -> visiter.booleanValue(false)
            "true" -> visiter.booleanValue(true)
            "null" -> visiter.nullValue()
            else -> {
                if (v.isNumber)
                    visiter.numberValue(v.toDouble())
                else
                    TODO("Unknown word '$v'")
            }
        }
    }

    private fun parseArray(arrayVisiter: JsonArrayVisiter) {
        arrayVisiter.start()
        while (true) {
            when (val c = reader.readNoSpace()) {
                ']' -> {
                    arrayVisiter.end()
                    return
                }
                ',' -> {
                }
                else -> {
                    reader.addFirst(c.toString().asReader())
                    accept(arrayVisiter.element())
                }
            }
        }
    }

    private fun parseObject(objectVisiter: JsonObjectVisiter) {
        objectVisiter.start()
        while (true) {
            when (val c = reader.readNoSpace()) {
                '}' -> {
                    objectVisiter.end()
                    return
                }
                '"' -> {
                    parseProperty(objectVisiter)
                }
                ',' -> {
                }
                else -> TODO("Unknown \"$c\"")
            }
        }
    }

    private fun parseProperty(objectVisiter: JsonObjectVisiter) {
        val name = parseString()
        if (reader.readNoSpace() != ':')
            TODO()
        println("Reading property $name")
        accept(objectVisiter.property(name))
    }

    private fun parseString(): String {
        val sb = StringBuilder()
        while (true) {
            var c = reader.read()
            if (c == '"')
                break
            if (c == '\\')
                c = reader.read()
            sb.append(c)
        }
        return sb.toString()
    }
}

private fun Reader.readNoSpace(): Char? {
    while (true) {
        try {
            val c = read()
            if (c != '\r' && c != '\n' && c != ' ')
                return c
        } catch (e: EOFException) {
            return null
        }
    }
}

private fun ComposeReader.skipSpaces() {
    while (true) {
        try {
            val c = read()
            if (c != ' ' && c != '\n' && c != '\r') {
                addFirst(c.toString().asReader())
                break
            }
        } catch (e: EOFException) {
            break
        }
    }
}

private fun ComposeReader.addFirst(char: Char): ComposeReader = addFirst(char.toString())
private fun ComposeReader.addFirst(text: String): ComposeReader = addFirst(text.asReader())

private fun ComposeReader.word(): String {
    skipSpaces()
    val sb = StringBuilder()
    while (true) {
        try {
            val c = read()
            if (c.isBreak && c != '.') {
                println("Break on '$c'")
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