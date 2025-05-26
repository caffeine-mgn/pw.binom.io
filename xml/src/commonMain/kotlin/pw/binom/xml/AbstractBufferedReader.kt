package pw.binom.xml

import pw.binom.xml.AbstractXmlTokenizer.EOFException


abstract class AbstractBufferedReader(bufferSize: Int) {
  protected var eof = false
  protected var internalPosition = 0

  protected var column = 0
  protected var line = 0
  protected val buffer = CharBuffer(bufferSize) {
    internalPosition--
  }

  val position
    get() = internalPosition

  fun push(value: Char) {
    buffer.push(value)
  }

  protected inline fun readChar(next:()->Char?): Char? {
    if (eof) {
      return null
    }
    val resultChar = if (buffer.isEmpty) {
      try {
        next()
      } catch (e: EOFException) {
        eof = true
        throw e
      }
    } else {
      buffer.get()
    }
    if (resultChar == null) {
      eof = true
      return null
    }
    internalPosition++
    return resultChar
  }
}
