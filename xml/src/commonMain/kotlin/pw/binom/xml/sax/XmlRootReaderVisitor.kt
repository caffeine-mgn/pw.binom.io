package pw.binom.xml.sax

import pw.binom.io.AsyncReader
import pw.binom.xml.AsyncXmlLexer
import pw.binom.xml.TokenType
import pw.binom.xml.nextSkipEmpty

class XmlRootReaderVisitor(val lexer: AsyncXmlLexer) {
    constructor(reader: AsyncReader) : this(AsyncXmlLexer(reader))

    suspend fun accept(visiter: AsyncXmlVisiter) {
        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.TAG_START) {
            throw ExpectedException("TAG_START")
        }

        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.QUESTION) {
            throw ExpectedException("QUESTION")
        }

        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.SYMBOL) {
            throw ExpectedException("SYMBOL")
        }
        if (lexer.text != "xml")
            TODO()
        while (true) {
            if (!lexer.next())
                TODO()
            if (lexer.tokenType == TokenType.QUESTION)
                break
        }

        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.TAG_END) {
            throw ExpectedException("TAG_END")
        }

        val root = AsyncXmlReaderVisitor(lexer)
        root.accept(visiter)
    }
}