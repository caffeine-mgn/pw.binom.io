package pw.binom.xml.sax

import pw.binom.collections.Stack
import pw.binom.io.Reader
import pw.binom.xml.SyncXmlLexer
import pw.binom.xml.TokenType
import pw.binom.xml.nextSkipEmpty

class SyncXmlReaderVisitor(val lexer: SyncXmlLexer) {
    constructor(reader: Reader) : this(SyncXmlLexer(reader))

    private class Record(val name: String, val visitor: SyncXmlVisitor)

    private val visitors = Stack<Record>().asLiFoQueue()

    /**
     * Чтение начала с <
     */
    private fun t1() {
        if (!lexer.next()) {
            TODO()
        }
        when (lexer.tokenType) {
            TokenType.SYMBOL -> t2()
            TokenType.SLASH -> closeTag()
            TokenType.EXCLAMATION -> readCDATA()

            else -> TODO("->${lexer.text}")
        }
    }

    /**
     * Чтение CDATA. Прочитанно "<!"
     */
    private fun readCDATA() {
        if (!lexer.next()) {
            TODO()
        }
        if (lexer.tokenType != TokenType.LEFT_BRACKET) {
            TODO()
        }
        if (!lexer.next()) {
            TODO()
        }
        if (lexer.tokenType != TokenType.SYMBOL) {
            TODO()
        }
        if (lexer.text != "CDATA") {
            TODO()
        }
        if (!lexer.next()) {
            TODO()
        }
        if (lexer.tokenType != TokenType.LEFT_BRACKET) {
            TODO()
        }
        val v = visitors.peek()
        val data = StringBuilder()
        while (true) {
            if (!lexer.next()) {
                TODO()
            }
            if (lexer.tokenType == TokenType.RIGHT_BRACKET) {
                if (!lexer.next()) {
                    TODO()
                }
                if (lexer.tokenType != TokenType.RIGHT_BRACKET) {
                    TODO()
                }
                if (!lexer.next()) {
                    TODO()
                }
                if (lexer.tokenType != TokenType.TAG_END) {
                    TODO()
                }
                v.visitor.cdata(data.toString())
                break
            }

            data.append(lexer.text)
        }
    }

    /**
     * Закрытие. "</" уже прочитано
     */
    private fun closeTag() {
        if (!lexer.next()) {
            TODO()
        }
        if (lexer.tokenType != TokenType.SYMBOL) {
            TODO()
        }
        val visitor = visitors.peek()
        val tagName = lexer.text
        if (visitor.name != tagName) {
            throw ExpectedException("Expected closing of tag [${visitor.name}] but got [$tagName]")
        }
        if (!lexer.next()) {
            TODO()
        }
        if (lexer.tokenType != TokenType.TAG_END) {
            TODO()
        }
        visitor.visitor.end()
        visitors.pop()
    }

    /**
     * Чтение тега. После < идёт какой-то текст
     */
    private fun t2() {
        val nodeName = lexer.text
        val subNode = visitors.peek().visitor.subNode(nodeName)
        visitors.push(Record(nodeName, subNode))
        subNode.start(nodeName)
        if (!lexer.nextSkipEmpty()) {
            TODO()
        }

        fun readAttribute() {
            val attribute = lexer.text
            if (!lexer.nextSkipEmpty()) {
                TODO()
            }
            if (lexer.tokenType != TokenType.EQUALS) {
                TODO("Обработка аттрибута без значения")
            }
            if (!lexer.nextSkipEmpty()) {
                TODO()
            }
            if (lexer.tokenType != TokenType.STRING) {
                TODO()
            }
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
                            if (lexer.tokenType != TokenType.TAG_END) {
                                TODO()
                            }
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
                if (lexer.tokenType != TokenType.TAG_END) {
                    TODO()
                }
                subNode.end()
                visitors.pop()
            }
            TokenType.TAG_END -> {
                accept()
            }
            else -> TODO()
        }
    }

    private fun accept() {
        var t = false
        while (lexer.next()) {
            when (lexer.tokenType) {
                TokenType.TAG_START -> {
                    t1()
                }
                else -> {
                    if (lexer.text.isBlank() && !t) {
                        continue
                    }
                    t = true
                    visitors.peek().visitor.value(lexer.text)
                }
            }
        }
    }

    fun accept(visitor: SyncXmlVisitor) {
        visitors.push(Record("ROOT", visitor))
        do {
            accept()
        } while (visitors.size > 1)
    }
}
