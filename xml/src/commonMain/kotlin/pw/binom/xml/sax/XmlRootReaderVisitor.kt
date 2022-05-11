package pw.binom.xml.sax

import pw.binom.io.AsyncReader
import pw.binom.io.EOFException
import pw.binom.xml.AsyncXmlLexer
import pw.binom.xml.TokenType
import pw.binom.xml.nextSkipEmpty

class XmlRootReaderVisitor(val lexer: AsyncXmlLexer) {
    constructor(reader: AsyncReader) : this(AsyncXmlLexer(reader))

    suspend fun accept(visiter: AsyncXmlVisitor) {
        if (!lexer.nextSkipEmpty()) {
            throw EOFException("on ${lexer.line}:${lexer.column}")
        }
        if (lexer.tokenType != TokenType.TAG_START) {
            throw ExpectedException("TAG_START")
        }

        if (!lexer.nextSkipEmpty()) {
            throw EOFException("on ${lexer.line}:${lexer.column}")
        }
        if (lexer.tokenType != TokenType.QUESTION) {
            throw ExpectedException("QUESTION")
        }

        if (!lexer.nextSkipEmpty()) {
            throw EOFException("on ${lexer.line}:${lexer.column}")
        }
        if (lexer.tokenType != TokenType.SYMBOL) {
            throw ExpectedException("SYMBOL")
        }
        if (lexer.text != "xml") {
            throw EOFException("on ${lexer.line}:${lexer.column}")
        }
        while (true) {
            if (!lexer.next()) {
                throw EOFException("on ${lexer.line}:${lexer.column}")
            }
            if (lexer.tokenType == TokenType.QUESTION) {
                break
            }
        }

        if (!lexer.nextSkipEmpty()) {
            throw EOFException("on ${lexer.line}:${lexer.column}")
        }
        if (lexer.tokenType != TokenType.TAG_END) {
            throw ExpectedException("TAG_END")
        }

        val root = AsyncXmlReaderVisitor(lexer)
        root.accept(visiter)
    }
}
