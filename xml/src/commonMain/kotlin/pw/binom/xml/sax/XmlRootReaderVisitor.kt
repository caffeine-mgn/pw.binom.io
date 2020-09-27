package pw.binom.xml.sax

import pw.binom.io.AsyncReader
import pw.binom.io.ComposeAsyncReader
import pw.binom.xml.AsyncXmlLexer
import pw.binom.xml.TokenType
import pw.binom.xml.nextSkipEmpty

class XmlRootReaderVisitor(val lexer: AsyncXmlLexer) {
    constructor(reader: AsyncReader) : this(AsyncXmlLexer(reader))

    suspend fun accept(visiter: XmlVisiter) {
        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.TAG_START) {
            TODO()
        }

        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.QUESTION) {
            TODO()
        }

        if (!lexer.nextSkipEmpty())
            TODO()
        if (lexer.tokenType != TokenType.SYMBOL) {
            TODO()
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
            TODO()
        }

        val root = XmlReaderVisiter(lexer)
        root.accept(visiter)
    }
}