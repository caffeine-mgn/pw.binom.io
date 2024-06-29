package pw.binom.xml.sax

import pw.binom.io.AsyncReader
import pw.binom.io.EOFException
import pw.binom.xml.AbstractXmlLexer
import pw.binom.xml.TokenType

class XmlRootReaderVisitor(val lexer: AbstractXmlLexer, val reader: AsyncReader) {

  private suspend fun AbstractXmlLexer.nextSkipEmpty() = nextSkipEmpty { reader.readChar() }
  private suspend fun AbstractXmlLexer.next() = commonNext { reader.readChar() }

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

    val root = AsyncXmlReaderVisitor(reader = reader, lexer = lexer)
    root.accept(visiter)
  }
}
