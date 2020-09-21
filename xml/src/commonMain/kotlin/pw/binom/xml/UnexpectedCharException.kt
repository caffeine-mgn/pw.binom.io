package pw.binom.xml

class UnexpectedCharException(val char: Char) : XmlException() {
    override val message: String?
        get() = "Unexpected char \"$char\""
}