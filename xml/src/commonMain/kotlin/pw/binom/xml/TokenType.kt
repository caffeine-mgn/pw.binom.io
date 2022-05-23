package pw.binom.xml

enum class TokenType {
    UNKNOWN,
    STRING,
    TAG_START,
    TAG_END,
    SLASH,
    EQUALS,
    EXCLAMATION, // !
    LEFT_BRACKET,
    RIGHT_BRACKET,
    AMPERSAND,
    SEMICOLON,
    QUESTION,
    SYMBOL,
    EMPTY,
    MINUS, // -
}
