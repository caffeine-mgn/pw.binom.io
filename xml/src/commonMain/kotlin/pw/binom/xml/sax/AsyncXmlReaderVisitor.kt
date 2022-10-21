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

    private var tagBodyBuilder: StringBuilder? = null

    /**
     * Чтение начала с <
     */
    private suspend fun readTagStart() {
        if (!lexer.next()) {
            throw EOFException()
        }
        when (lexer.tokenType) {
            TokenType.SYMBOL -> readTagAttributes()
            TokenType.SLASH -> closeTag()
            TokenType.EXCLAMATION -> readCDATAOrComment()

            else -> throw IllegalStateException("Unknown token type: ${lexer.tokenType}")
        }
    }

    private suspend fun readCDATAOrComment() {
        if (!lexer.next()) TODO()
        return when (lexer.tokenType) {
            TokenType.LEFT_BRACKET -> readCDATA()
            TokenType.MINUS -> readComment()
            else -> TODO("Unknown token \"${lexer.text}\" on ${lexer.line + 1}:${lexer.column - lexer.text.length}")
        }
    }

    /**
     * Чтение комментариев. Прочитано "<-"
     */
    private suspend fun readComment() {
        if (!lexer.next()) {
            TODO("Неожиданный конец комментария")
        }
        if (lexer.tokenType != TokenType.MINUS) {
            TODO()
        }
        val sb = StringBuilder()
        while (true) {
            if (!lexer.next()) {
                TODO("Неожиданный конец коментария")
            }
            if (lexer.tokenType == TokenType.MINUS) { // возможно это конец
                if (!lexer.next()) {
                    TODO("Неожиданный конец коментария")
                }
                if (lexer.tokenType == TokenType.MINUS) { // да, это конец
                    if (!lexer.next()) {
                        TODO("Неожиданный конец коментария")
                    }
                    if (lexer.tokenType != TokenType.TAG_END) {
                        TODO()
                    }
                    visitors.peek().visitor.comment(sb.toString())
                    return
                } else { // нет, это просто тире в комментарии
                    sb.append("-").append(lexer.text)
                }
                //
            } else {
                sb.append(lexer.text)
            }
        }
    }

    /**
     * Чтение CDATA. Прочитано "<!"
     */
    private suspend fun readCDATA() {
        if (lexer.tokenType != TokenType.LEFT_BRACKET) TODO("lexer.tokenType=${lexer.tokenType} ${lexer.line}:${lexer.column} ${lexer.text}")
        if (!lexer.next()) TODO()
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
                throw EOFException("on line ${lexer.line}:${lexer.column}")
            }
            if (lexer.tokenType == TokenType.RIGHT_BRACKET) {
                if (!lexer.next()) {
                    throw XMLSAXException("Unexpected end of xml document on ${lexer.line + 1}:${lexer.column}")
                }
                if (lexer.tokenType != TokenType.RIGHT_BRACKET) {
                    throw XMLSAXException("Unknown text \"${lexer.text}\" with type ${lexer.tokenType} on ${lexer.line + 1}:${lexer.column}")
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
    private suspend fun closeTag() {
        if (!lexer.next()) {
            TODO()
        }

        if (lexer.tokenType != TokenType.SYMBOL) {
            TODO()
        }
        val visitor = visitors.peek()
        val tagName = readTagName()
        if (visitor.name != tagName) {
            throw ExpectedException("Expected closing of tag [${visitor.name}] but got [$tagName]")
        }
//        if (!lexer.next()) {
//            TODO()
//        }
        if (lexer.tokenType != TokenType.TAG_END) {
            TODO()
        }
        if (tagBodyBuilder != null) {
            visitors.peek().visitor.value(tagBodyBuilder!!.toString())
            tagBodyBuilder = null
        }
        visitor.visitor.end()
        visitors.pop()
    }

    private suspend fun readTagName(): String {
        val sb = StringBuilder()
        sb.append(lexer.text)
        while (true) {
            if (!lexer.next()) {
                TODO()
            }
            when (lexer.tokenType) {
                TokenType.MINUS, TokenType.SYMBOL -> sb.append(lexer.text)
                TokenType.EMPTY, TokenType.SLASH, TokenType.TAG_END -> break
                else -> throw XMLSAXException("Can't read tag name. Unknown text \"${lexer.text}\" with type ${lexer.tokenType} on ${lexer.line + 1}:${lexer.column}")
            }
        }
        return sb.toString()
    }

    /**
     * Чтение тега. После < идёт какой-то текст
     */
    private suspend fun readTagAttributes() {
        val nodeName = readTagName()
        val subNode = visitors.peek().visitor.subNode(nodeName)
        visitors.push(Record(nodeName, subNode))
        subNode.start()
//        if (!lexer.nextSkipEmpty()) {
//            TODO()
//        }
        do {
            if (lexer.tokenType != TokenType.EMPTY) {
                break
            }
        } while (lexer.next())

        suspend fun readAttribute() {
            val attribute = lexer.text
            if (!lexer.nextSkipEmpty()) TODO()
            if (lexer.tokenType != TokenType.EQUALS) {
                TODO("Обработка аттрибута без значения. Ожидалось \"=\", а по факту ${lexer.text} ${lexer.tokenType}")
            }
            if (!lexer.nextSkipEmpty()) TODO()
            if (lexer.tokenType != TokenType.STRING) {
                TODO("${lexer.line + 1}:${lexer.column}")
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
                tagBodyBuilder = StringBuilder()
//                subNode.end()
//                visitors.pop()
//                println("OK!")
                // accept()
            }
            else -> throw XMLSAXException("Unknown text token \"${lexer.text}\" with type ${lexer.tokenType} on ${lexer.line + 1}:${lexer.column}")
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
                    if (lexer.text.isBlank() && !t) {
                        continue
                    }
                    t = true
                    if (tagBodyBuilder == null) {
                        tagBodyBuilder = StringBuilder()
                    }
                    tagBodyBuilder!!.append(lexer.text)
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
