package pw.binom.xml

class ExpectedException(val tag: String) : XMLSAXException() {
    override val message: String?
        get() = "Expected \"$tag\""
}