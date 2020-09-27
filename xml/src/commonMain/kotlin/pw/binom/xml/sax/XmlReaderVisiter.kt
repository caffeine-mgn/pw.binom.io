package pw.binom.xml.sax

import pw.binom.Stack
import pw.binom.io.*
import pw.binom.xml.AsyncXmlLexer
import pw.binom.xml.TokenType
import pw.binom.xml.nextSkipEmpty

class XmlReaderVisiter(val lexer: AsyncXmlLexer) {
    constructor(reader: AsyncReader) : this(AsyncXmlLexer(reader))

    private class Record(val name: String, val visiter: XmlVisiter)

    private val visitors = Stack<Record>().asLiFoQueue()

    /**
     * Чтение начала с <
     */
    private suspend fun t1() {
        if (!lexer.next()) {
            TODO()
        }
        when (lexer.tokenType) {
            TokenType.SYMBOL -> t2()
            TokenType.SLASH -> closeTag()
            TokenType.EXCLAMATION -> readCDATA()

            else -> TODO()
        }
    }

    /**
     * Чтение CDATA. Прочитанно "<!"
     */
    private suspend fun readCDATA() {
        if (!lexer.next())
            TODO()
        if (lexer.tokenType != TokenType.LEFT_BRACKET)
            TODO()
        if (!lexer.next())
            TODO()
        if (lexer.tokenType != TokenType.SYMBOL)
            TODO()
        if (lexer.text != "CDATA")
            TODO()
        if (!lexer.next())
            TODO()
        if (lexer.tokenType != TokenType.LEFT_BRACKET)
            TODO()
        val v = visitors.peek()
        val data = StringBuilder()
        while (true) {
            if (!lexer.next())
                TODO()
            if (lexer.tokenType == TokenType.RIGHT_BRACKET) {
                if (!lexer.next())
                    TODO()
                if (lexer.tokenType != TokenType.RIGHT_BRACKET)
                    TODO()
                if (!lexer.next())
                    TODO()
                if (lexer.tokenType != TokenType.TAG_END)
                    TODO()
                v.visiter.cdata(data.toString())
                break
            }

            data.append(lexer.text)
        }
    }

    /**
     * Закрытие. "</" уже прочитано
     */
    private suspend fun closeTag() {
        if (!lexer.next())
            TODO()
        if (lexer.tokenType != TokenType.SYMBOL)
            TODO()
        val visitor = visitors.peek()
        val tagName = lexer.text
        if (visitor.name != tagName) {
            throw ExpectedException("Expected closing of tag [${visitor.name}] but got [$tagName]")
        }
        if (!lexer.next())
            TODO()
        if (lexer.tokenType != TokenType.TAG_END)
            TODO()
        visitor.visiter.end()
        visitors.pop()
    }

    /**
     * Чтение тега. После < идёт какой-то текст
     */
    private suspend fun t2() {
        val nodeName = lexer.text
        val subNode = visitors.peek().visiter.subNode(nodeName)
        visitors.push(Record(nodeName, subNode))
        subNode.start()
        if (!lexer.nextSkipEmpty()) {
            TODO()
        }

        suspend fun readAttribute() {
            val attribute = lexer.text
            if (!lexer.nextSkipEmpty())
                TODO()
            if (lexer.tokenType != TokenType.EQUALS) {
                TODO("Обработка аттрибута без значения")
            }
            if (!lexer.nextSkipEmpty())
                TODO()
            if (lexer.tokenType != TokenType.STRING)
                TODO()
            val value = lexer.text.removePrefix("\"").removeSuffix("\"")
            subNode.attribute(
                    name = attribute,
                    value = value
            )
        }

        when (lexer.tokenType) {
            TokenType.SYMBOL -> {
                while (true) {
                    readAttribute()
                    if (!lexer.nextSkipEmpty()) {
                        TODO()
                    }
                    when (lexer.tokenType) {
                        TokenType.SYMBOL -> continue
                        TokenType.TAG_END -> break
                        TokenType.SLASH -> {
                            if (!lexer.nextSkipEmpty()) {
                                TODO()
                            }
                            if (lexer.tokenType != TokenType.TAG_END)
                                TODO()
                            subNode.end()
                            visitors.pop()
                            break
                        }
                        else -> TODO()
                    }
                }
            }
            TokenType.SLASH -> {
                if (!lexer.nextSkipEmpty()) {
                    TODO()
                }
                if (lexer.tokenType != TokenType.TAG_END)
                    TODO()
                subNode.end()
                visitors.pop()
            }
            TokenType.TAG_END -> {
                accept()
            }
            else -> TODO()
        }
    }

    private suspend fun accept() {

        var t = false
        while (lexer.next()) {
            when (lexer.tokenType) {
                TokenType.TAG_START -> {
                    t1()
                }
                else -> {
                    if (lexer.text.isBlank() && !t)
                        continue
                    t = true
                    visitors.peek().visiter.value(lexer.text)
                }
            }
        }

//        when (val char = reader.readNoSpace()) {
//            '<' -> {
//                if (reader.isNextChar { it == '!' }) {
//                    reader.addFirst("<!")
//                    visiters.peek().visiter.cdata(readCDATA())
//                } else
//                    readTag()
//            }
//            null -> {
//            }
//            else -> {
//                reader.addFirst(char)
//                visiters.peek().visiter.value(readTextBody())
//            }
//        }
    }

    suspend fun accept(visiter: XmlVisiter) {
        visitors.push(Record("ROOT", visiter))
        do {
            accept()
        } while (visitors.size > 1)
    }
/*
    private suspend fun readCDATA(): String {
        if (!reader.readText("<![CDATA[")) {
            throw ExpectedException("<![CDATA[")
        }
        val sb = StringBuilder()
        while (true) {
            if (reader.readText("]]>"))
                break
            val c = reader.read()
            sb.append(c)
        }
        return sb.toString()
    }

    private suspend fun readTextBody(): String {
        val sb = StringBuilder()
        while (true) {
            if (reader.isNextChar { it == '<' })
                break
            sb.append(reader.read())
        }
        return sb.toString()
    }

    private suspend fun readTag() {
        reader.skipSpaces()
        if (reader.isNextChar { it == '/' }) {
            reader.read()
            val endTagName = reader.word()
            val lastNode = visiters.peek()
            if (lastNode.name != endTagName) {
                throw ExpectedException(lastNode.name)
            }

            val bb = visiters.pop()
            bb.visiter.end()
            reader.skipSpaces()
            if (!reader.readText(">"))
                throw ExpectedException(">")
            return
        }

        val nodeName = reader.word()
        val subNode = visiters.peek().visiter.subNode(nodeName)
        subNode.start()
        visiters.push(Record(nodeName, subNode))

        while (true) {
            val vvv = visiters.peek().visiter
            reader.skipSpaces()
            if (reader.isNextChar { it == '/' }) {
                reader.read()
                reader.skipSpaces()
                if (!reader.readText(">"))
                    ExpectedException(">")
                vvv.end()
                visiters.pop()
                return
            }
            if (reader.isNextChar { it == '>' }) {
                reader.read()
                accept()
                return
            }
            val attrName = reader.word()
            reader.skipSpaces()
            val nn = reader.read()
            if (nn != '=')
                throw ExpectedException("=")
            subNode.attribute(attrName, reader.readString())
        }
    }
    */
}
/*
internal suspend fun ComposeAsyncReader.readString(): String {
    if (read() != '"')
        throw ExpectedException("\"")
    val sb = StringBuilder()
    while (true) {
        try {
            val c = read()
            if (c == '"')
                break
            sb.append(c)
        } catch (e: EOFException) {
        }
    }
    return sb.toString()
}

private suspend fun ComposeAsyncReader.readNoSpace(): Char? {
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

private val Char.isBreak: Boolean
    get() = this == '\n' || this == '\r' || this == ' ' || this == '=' || this == '<' || this == '>' || this == '/'

internal suspend fun ComposeAsyncReader.readText(text: String): Boolean {
    val sb = StringBuilder()
    while (true) {
        if (text.length == sb.length)
            return true
        val c = try {
            read()
        } catch (e: EOFException) {
            addFirst(sb.toString())
            return false
        }
        sb.append(c)
        if (text[sb.length - 1] != c) {
            addFirst(sb.toString())
            return false
        }
    }
}

private suspend fun ComposeAsyncReader.isNextChar(charFunc: (Char) -> Boolean): Boolean {
    return try {
        val c = read() ?: return false
        addFirst(c)
        charFunc(c)
    } catch (e: EOFException) {
        false
    }

}

internal suspend fun ComposeAsyncReader.skipSpaces() {
    while (true) {
        try {
            val c = read()
            if (c != ' ' && c != '\n' && c != '\r') {
                addFirst(c.toString().asReader().asAsync())
                break
            }
        } catch (e: EOFException) {
            break
        }
    }
}

private fun ComposeAsyncReader.addFirst(char: Char) = addFirst(char.toString())
private fun ComposeAsyncReader.addFirst(text: String) = addFirst(text.asReader().asAsync())

internal suspend fun ComposeAsyncReader.word(): String {
    skipSpaces()
    val sb = StringBuilder()
    while (true) {
        try {
            val c = read() ?: return sb.toString()
            if (c.isBreak && c != '.') {
                addFirst(c)
                return sb.toString()
            }
            sb.append(c)
        } catch (e: EOFException) {
            return sb.toString()
        }
    }
}
*/