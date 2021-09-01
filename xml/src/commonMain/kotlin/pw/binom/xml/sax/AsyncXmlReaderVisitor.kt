package pw.binom.xml.sax

import pw.binom.Stack
import pw.binom.io.AsyncReader
import pw.binom.io.EOFException
import pw.binom.xml.AsyncXmlLexer
import pw.binom.xml.TokenType
import pw.binom.xml.nextSkipEmpty

class AsyncXmlReaderVisitor(val lexer: AsyncXmlLexer) {
    constructor(reader: AsyncReader) : this(AsyncXmlLexer(reader))

    private class Record(val name: String, val visitor: AsyncXmlVisitor)

    private val visitors = Stack<Record>().asLiFoQueue()

    /**
     * Чтение начала с <
     */
    private suspend fun readTagStart() {
        if (!lexer.next()) {
            throw EOFException()
        }
        when (lexer.tokenType) {
            TokenType.SYMBOL -> readTagBody()
            TokenType.SLASH -> closeTag()
            TokenType.EXCLAMATION -> readCDATA()

            else -> throw IllegalStateException("Unknown token type: ${lexer.tokenType}")
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
                v.visitor.cdata(data.toString())
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
        visitor.visitor.end()
        visitors.pop()
    }

    /**
     * Чтение тега. После < идёт какой-то текст
     */
    private suspend fun readTagBody() {
        val nodeName = lexer.text
        val subNode = visitors.peek().visitor.subNode(nodeName)
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
//                subNode.end()
//                visitors.pop()
//                println("OK!")
                //accept()
            }
            else -> TODO()
        }
    }

    private suspend fun accept() {
        var t = false
        while (lexer.next()) {
            when (lexer.tokenType) {
                TokenType.TAG_START -> {
                    readTagStart()
                }
                else -> {
                    if (lexer.text.isBlank() && !t)
                        continue
                    t = true
                    visitors.peek().visitor.value(lexer.text)
                }
            }
        }
    }

    suspend fun accept(visitor: AsyncXmlVisitor) {
        visitors.push(Record("ROOT", visitor))
        do {
            accept()
        } while (visitors.size > 1)
    }
}