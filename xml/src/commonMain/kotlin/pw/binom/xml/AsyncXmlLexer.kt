package pw.binom.xml

import pw.binom.io.AsyncReader

class AsyncXmlLexer(val reader: AsyncReader) {
    var column = 0
        private set

    var line = 0
        private set

    var position = 0
        private set

    var isEof = false
        private set

    var tokenType = TokenType.UNKNOWN
        private set

    private var lastChar: Char? = null
    private val sb = StringBuilder()
    var text = ""
        private set

    suspend fun next(): Boolean {
        if (isEof) {
            return false
        }
        val char = lastChar ?: reader.readChar()
        if (char == null) {
            isEof = true
            return false
        }
        lastChar = null
        when {
            char == '<' -> {
                position++
                column++
                tokenType = TokenType.TAG_START
                text = "<"
                return true
            }
            char == '>' -> {
                position++
                column++
                tokenType = TokenType.TAG_END
                text = ">"
                return true
            }
            char == '/' -> {
                position++
                column++
                tokenType = TokenType.SLASH
                text = "/"
                return true
            }
            char == '!' -> {
                position++
                column++
                text = "!"
                tokenType = TokenType.EXCLAMATION
                return true
            }
            char == '[' -> {
                position++
                column++
                text = "["
                tokenType = TokenType.LEFT_BRACKET
                return true
            }
            char == ']' -> {
                position++
                column++
                text = "]"
                tokenType = TokenType.RIGHT_BRACKET
                return true
            }

            char == '=' -> {
                position++
                column++
                text = "="
                tokenType = TokenType.EQUALS
                return true
            }
            char == '&' -> {
                position++
                column++
                tokenType = TokenType.AMPERSAND
                text = "&"
                return true
            }
            char == '-' -> {
                position++
                column++
                tokenType = TokenType.MINUS
                text = "-"
                return true
            }
            char == ';' -> {
                position++
                column++
                tokenType = TokenType.SEMICOLON
                text = ";"
                return true
            }
            char == '"' || char == '\'' -> {
                text = readString(char)
                return true
            }
            char == '?' -> {
                position++
                column++
                tokenType = TokenType.QUESTION
                text = "?"
                return true
            }
            char.isEmpty -> {
                text = readEmptySpace(char)
                return true
            }
            else -> {
                return readSymbol(char)
            }
        }
    }

    private suspend fun readSymbol(char: Char): Boolean {
        column++
        position++
        sb.clear()
        sb.append(char)
        tokenType = TokenType.SYMBOL
        while (true) {
            val c = reader.readChar()
            if (c == null) {
                isEof = true
                text = sb.toString()
                sb.clear()
                return true
            }
            when (c) {
                '[', ']', '!', '<', '>', '/', '=', '"', '?', '&', ';', '\n', ' ', '\r', '\t' -> {
                    lastChar = c
                    text = sb.toString()
                    sb.clear()
                    return true
                }
                else -> {
                    column++
                    position++
                    sb.append(c)
                }
            }
        }
    }

    private suspend fun readString(firstChar: Char): String {
        column++
        position++
        sb.append(firstChar)
        tokenType = TokenType.STRING
        while (true) {
            val c = reader.readChar()
            if (c == null) {
                isEof = true
                lastChar = null
                throw UnexpectedEOFException()
            }
            when (c) {
                firstChar -> {
                    column++
                    position++
                    lastChar = null
                    sb.append(firstChar)
                    val char = sb.toString()
                    sb.clear()
                    return char
                }
                else -> {
                    position++
                    if (c == '\n') {
                        line++
                        column = 0
                    } else {
                        column++
                    }
                    lastChar = null
                    sb.append(c)
                }
            }
        }
    }

    private suspend fun readEmptySpace(startChar: Char): String {
        sb.clear()
        position++
        if (startChar == '\n') {
            line++
            column = 0
        } else {
            column++
        }
        sb.append(startChar)

        while (true) {
            val c = reader.readChar()
            when {
                c != null && c.isEmpty -> {
                    position++
                    if (c == '\n') {
                        line++
                        column = 0
                    } else {
                        column++
                    }
                    sb.append(c)
                }
                else -> {
                    lastChar = c
                    if (c == null) {
                        isEof = true
                    }
                    val c = sb.toString()
                    sb.clear()
                    tokenType = TokenType.EMPTY
                    return c
                }
            }
        }
    }
}

val Char.isEmpty
    get() = this == '\n' || this == ' ' || this == '\r' || this == '\t'

suspend fun AsyncXmlLexer.nextSkipEmpty(): Boolean {
    while (next()) {
        if (tokenType != TokenType.EMPTY)
            return true
    }
    return false
}
