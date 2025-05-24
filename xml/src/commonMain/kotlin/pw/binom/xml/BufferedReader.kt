package pw.binom.xml

import pw.binom.io.Reader
import pw.binom.xml.XmlTokenizer.EOFException

class BufferedReader(val reader: Reader, bufferSize: Int) : Reader {
  private var eof = false
  private var column = 0
  private var line = 0
  private val buffer = CharBuffer(bufferSize) {
    internalPosition--
  }
  private var internalPosition = 0
  val position
    get() = internalPosition

  fun push(value: Char) {
    buffer.push(value)
  }

  override fun read(): Char? {
    if (eof) {
      return null
    }
    val resultChar = if (buffer.isEmpty) {
      try {
        reader.read()
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

  override fun close() {
    reader.close()
  }
}
