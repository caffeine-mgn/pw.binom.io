package pw.binom.xml

class UnexpectedEOFException : XmlException() {
    override val message: String?
        get() = "Unexpected end of data"
}
