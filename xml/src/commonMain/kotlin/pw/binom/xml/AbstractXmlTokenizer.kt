package pw.binom.xml

internal typealias NextCharFunc = () -> Char

abstract class AbstractXmlTokenizer {
  companion object {
    inline fun readString(value: String, readChar: () -> Char, pushBack: (Char) -> Unit): Boolean {
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

  protected abstract val reader: AbstractBufferedReader

  protected var internalTokenType = TokenType2.TAG_START
  protected var internalText = ""

  val type
    get() = internalTokenType
  val text
    get() = internalText

  class EOFException : Exception()

  protected var eof = false

  protected inline fun readString(value: String, readChar: NextCharFunc) =
    readString(
      value = value,
      readChar = readChar,
      pushBack = { reader.push(it) }
    )

  protected inline fun next(readChar: NextCharFunc): Boolean {
    if (eof) {
      return false
    }
    val char = try {
      readChar()
    } catch (e: EOFException) {
      return false
    }
    if (readStartConfig(char, readChar)) {
      return true
    }
    if (readEndConfig(char, readChar)) {
      return true
    }
    if (readSlash(char)) {
      return true
    }
    if (readComment(char, readChar)) {
      return true
    }
    if (readCDATA(char, readChar)) {
      return true
    }
    if (readTagStart(char)) {
      return true
    }
    if (readSymbol(char, readChar)) {
      return true
    }
    if (readWhitespace(char, readChar)) {
      return true
    }
    if (readEqual(char)) {
      return true
    }
    if (readString(char, readChar)) {
      return true
    }
    if (readTagEnd(char)) {
      return true
    }
    if (readPlaneText(char, readChar)) {
      return true
    }
    TODO("--->$char")
  }

  protected fun readSlash(char: Char): Boolean {
    if (char != '/') {
      return false
    }
    internalTokenType = TokenType2.SLASH
    internalText = "/"
    return true
  }

  protected inline fun readPlaneText(char: Char, readChar: NextCharFunc): Boolean {
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

  protected fun readTagEnd(char: Char): Boolean {
    if (char != '>') {
      return false
    }
    internalTokenType = TokenType2.TAG_END
    internalText = ">"
    return true
  }

  protected inline fun readString(char: Char, readChar: NextCharFunc): Boolean {
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

  protected fun readEqual(char: Char): Boolean {
    if (char != '=') {
      return false
    }
    internalTokenType = TokenType2.EQUAL
    internalText = "="
    return true
  }

  protected inline fun readWhitespace(char: Char, readChar: NextCharFunc): Boolean {
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

  protected fun readTagStart(char: Char): Boolean {
    if (char != '<') {
      return false
    }
    internalText = "<"
    internalTokenType = TokenType2.TAG_START
    return true
  }

  protected inline fun readSymbol(char: Char, readChar: NextCharFunc): Boolean {
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

  protected inline fun readCDATA(char: Char, readChar: NextCharFunc): Boolean {
    if (char != '<') {
      return false
    }
    if (!readString("![CDATA[", readChar)) {
      return false
    }
    val sb = StringBuilder()
    sb.append("<![CDATA[")
    while (true) {
      val r = readChar()
      if (r == ']') {
        if (readString("]>", readChar)) {
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

  protected inline fun readEndConfig(char: Char, readChar: NextCharFunc): Boolean {
    if (char != '?') {
      return false
    }
    if (!readString(">", readChar)) {
      return false
    }
    internalTokenType = TokenType2.CONFIG_END
    internalText = "?>"
    return true
  }

  protected inline fun readStartConfig(char: Char, readChar: NextCharFunc): Boolean {
    if (char != '<') {
      return false
    }
    if (!readString("?", readChar)) {
      return false
    }
    internalTokenType = TokenType2.CONFIG_START
    internalText = "<?"
    return true
  }

  // `<!--`
  protected inline fun readComment(char: Char, readChar: NextCharFunc): Boolean {
    if (char != '<') {
      return false
    }
    if (!readString("!--", readChar)) {
      return false
    }
    val sb = StringBuilder()
    sb.append("<!--")
    while (true) {
      val r = readChar()
      if (r == '-') {
        if (readString("->", readChar)) {
          sb.append("-->")
          break
        }
      }
      sb.append(r)
    }

    internalText = sb.toString()
    internalTokenType = TokenType2.COMMENT
    return true
  }
}
