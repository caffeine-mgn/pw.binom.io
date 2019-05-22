package pw.binom.xml.sax

class ExpectedException(val tag: String) : XMLSAXException() {
    override val message: String?
        get() = "Expected \"$tag\""
}