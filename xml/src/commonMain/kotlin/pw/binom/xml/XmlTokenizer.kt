package pw.binom.xml

import pw.binom.io.Reader

class XmlTokenizer(val reader: BufferedReader) {
  constructor(reader: Reader) : this(BufferedReader(reader, 10))

  companion object {
    fun readString(value: String, readChar: () -> Char, pushBack: (Char) -> Unit): Boolean {
      var pos = -1
      while (pos < value.length - 1) {
        val r = readChar()
        if (r == value[pos + 1]) {
          pos++
        } else {
          pushBack(r)
          while (pos >= 0) {
            pushBack(value[pos])
            pos--
          }
          return false
        }
      }
      return true
    }
  }

  //  private var position = -1
  private var eof = false

  class EOFException : Exception()

  private enum class State {
    NONE,
    COMMENT,
    STRING,
  }

  private var state = State.NONE
  private var internalTokenType = TokenType2.TAG_START
  private var internalText = ""
  val type
    get() = internalTokenType
  val text
    get() = internalText

//  private val buffer = CharBuffer(4) {
//    position--
//  }

  private fun nextChar(): Char =
    reader.read() ?: throw EOFException()

  private fun readChar(): Char {
    return nextChar()
//    val resultChar = if (buffer.isEmpty) {
//      try {
//        nextChar()
//      } catch (e: EOFException) {
//        eof = true
//        throw e
//      }
//    } else {
//      buffer.get()
//    }
//    position++
//    return resultChar
  }

  fun next(): Boolean {
    if (eof) {
      return false
    }
    val char = try {
      readChar()
    } catch (e: EOFException) {
      return false
    }
    if (readStartConfig(char)) {
      return true
    }
    if (readEndConfig(char)) {
      return true
    }
    if (readSlash(char)) {
      return true
    }
    if (readComment(char)) {
      return true
    }
    if (readCDATA(char)) {
      return true
    }
    if (readTagStart(char)) {
      return true
    }
    if (readSymbol(char)) {
      return true
    }
    if (readWhitespace(char)) {
      return true
    }
    if (readEqual(char)) {
      return true
    }
    if (readString(char)) {
      return true
    }
    if (readTagEnd(char)) {
      return true
    }
    if (readPlaneText(char)) {
      return true
    }
    TODO("--->$char")
  }

  private fun readSlash(char: Char): Boolean {
    if (char != '/') {
      return false
    }
    internalTokenType = TokenType2.SLASH
    internalText = "/"
    return true
  }

  private fun readPlaneText(char: Char): Boolean {
    if (char == '<' || char == '>') {
      return false
    }
    val sb = StringBuilder()
    sb.append(char)
    while (true) {
      val r = try {
        readChar()
      } catch (e: EOFException) {
        break
      }
      if (r == '<' || r == '>') {
        reader.push(r)
        break
      }
      sb.append(r)
    }
    internalTokenType = TokenType2.TEXT
    internalText = sb.toString()
    return true
  }

  private fun readTagEnd(char: Char): Boolean {
    if (char != '>') {
      return false
    }
    internalTokenType = TokenType2.TAG_END
    internalText = ">"
    return true
  }

  private fun readString(char: Char): Boolean {
    if (char != '"' && char != '\'') {
      return false
    }
    val sb = StringBuilder()
    sb.append(char)
    while (true) {
      val r = readChar()
      sb.append(r)
      if (r == char) {
        break
      }
    }
    internalTokenType = TokenType2.STRING
    internalText = sb.toString()
    return true
  }

  private fun readEqual(char: Char): Boolean {
    if (char != '=') {
      return false
    }
    internalTokenType = TokenType2.EQUAL
    internalText = "="
    return true
  }

  private fun readWhitespace(char: Char): Boolean {
    if (!char.isWhitespace()) {
      return false
    }
    val sb = StringBuilder()
    sb.append(char)
    while (true) {
      val r = readChar()
      if (!r.isWhitespace()) {
        reader.push(r)
        break
      }
      sb.append(r)
    }
    internalTokenType = TokenType2.WHITESPACE
    internalText = sb.toString()
    return true
  }

  private fun readSymbol(char: Char): Boolean {
    if (!char.isLetter()) {
      return false
    }
    val sb = StringBuilder()
    sb.append(char)
    while (true) {
      val r = readChar()
      if (r.isLetterOrDigit() || r == ':' || r == '_' || r == '.' || r == '-') {
        sb.append(r)
        continue
      }
      reader.push(r)
      break
    }
    internalText = sb.toString()
    internalTokenType = TokenType2.SYMBOL
    return true
  }

  private fun readTagStart(char: Char): Boolean {
    if (char != '<') {
      return false
    }
//    val sb = StringBuilder()
//    sb.append("<")
//    val c = readChar()
//    if (!c.isLetter()) {
//      buffer.push(c)
//      return false
//    }
//    sb.append(c)
//    while (true) {
//      val r = readChar()
//      if (r.isLetterOrDigit() || r == ':' || r == '_' || r == '.' || r == '-') {
//        sb.append(r)
//        continue
//      }
//      buffer.push(r)
//      break
//    }
    internalText = "<"
    internalTokenType = TokenType2.TAG_START
    return true
  }

  private val Char.isWhitespace: Boolean
    get() = this == ' ' || this == '\t' || this == '\r' || this == '\n'

  private inline fun readString(value: String) =
    readString(
      value = value,
      readChar = { readChar() },
      pushBack = { reader.push(it) }
    )

  private fun readCDATA(char: Char): Boolean {
    if (char != '<') {
      return false
    }
    if (!readString("![CDATA[")) {
      return false
    }
    val sb = StringBuilder()
    sb.append("<![CDATA[")
    while (true) {
      val r = readChar()
      if (r == ']') {
        if (readString("]>")) {
          sb.append("]]>")
          break
        }
      }
      sb.append(r)
    }
    internalText = sb.toString()
    internalTokenType = TokenType2.CDATA
    return true
  }

  private fun readEndConfig(char: Char): Boolean {
    if (char != '?') {
      return false
    }
    if (!readString(">")) {
      return false
    }
    internalTokenType = TokenType2.CONFIG_END
    internalText = "?>"
    return true
  }

  private fun readStartConfig(char: Char): Boolean {
    if (char != '<') {
      return false
    }
    if (!readString("?")) {
      return false
    }
    internalTokenType = TokenType2.CONFIG_START
    internalText = "<?"
    return true
  }

  // `<!--`
  private fun readComment(char: Char): Boolean {
    if (char != '<') {
      return false
    }
    if (!readString("!--")) {
      return false
    }
    val sb = StringBuilder()
    sb.append("<!--")
    while (true) {
      val r = readChar()
      if (r == '-') {
        if (readString("->")) {
          sb.append("-->")
          break
        }
      }
      sb.append(r)
    }

    internalText = sb.toString()
    internalTokenType = TokenType2.COMMENT
    return true
//    val f1 = readChar()
//    if (f1 != '!') {
//      buffer.push(f1)
//      return false
//    }
//    val f2 = readChar()
//    if (f2 != '-') {
//      buffer.push(f2)
//      buffer.push('!')
//      return false
//    }
//    val f3 = readChar()
//    if (f3 != '-') {
//      buffer.push(f3)
//      buffer.push('-')
//      buffer.push('!')
//      return false
//    }
//    return true
  }
}
