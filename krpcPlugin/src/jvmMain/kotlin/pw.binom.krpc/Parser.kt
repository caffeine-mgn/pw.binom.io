package pw.binom.krpc

import pw.binom.io.ComposeReader
import pw.binom.io.EOFException
import pw.binom.io.Reader
import pw.binom.io.asReader

class Parser(val reader: Reader) {
    private val buffer = ComposeReader().addLast(reader)

    private var end = false
    fun nextToken(): String? {
        if (end)
            return null
        val c = buffer.read()
        if (c == null) {
            end = true
            return null
        }

        return when {
            c.isSpace -> {
                buffer.addFirst(c.toString().asReader())
                readSpace()
            }
            c.isBrake -> {
                c.toString()
            }
            else -> {
                buffer.addFirst(c.toString().asReader())
                readWord()
            }
        }
    }

    fun back(str: String) {
        buffer.addFirst(str.asReader())
    }

    fun nextTokenNoSpace(): String? {
        while (true) {
            val str = nextToken() ?: return null
            if (str.isEmpty() || str[0].isSpace)
                continue
            return str
        }
    }

    private fun readWord(): String {
        val sb = StringBuilder()
        while (true) {
            val c = buffer.read() ?: break
            if (c.isBrake || c.isSpace) {
                buffer.addFirst(c.toString().asReader())
                break
            }
            sb.append(c)
        }
        return sb.toString()
    }

    private fun readSpace(): String {
        val sb = StringBuilder()
        while (true) {
            val c = buffer.read() ?: break
            if (c.isSpace)
                sb.append(c)
            else {
                buffer.addFirst(c.toString().asReader())
                break
            }
        }
        return sb.toString()
    }
}

private val Char.isSpace
    get() = this == ' ' || this == '\n' || this == '\t' || this == '\r'

private val Char.isBrake
    get() = this == '{' || this == '}' || this == '[' || this == ']' || this == ',' || this == '<' ||
            this == '>' || this == '?' || this == '.' || this == '/' || this == '*' || this == '(' ||
            this == ')' || this == ':'

private val Char.isWord
    get() = this in ('A'..'Z') || this in ('a'..'z')

private val Char.isNumber
    get() = this in ('0'..'9')

private fun ComposeReader.readNoSpace(): Char? {
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