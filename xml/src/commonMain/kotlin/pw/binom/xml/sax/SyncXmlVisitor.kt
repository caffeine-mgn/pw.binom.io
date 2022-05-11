package pw.binom.xml.sax

interface SyncXmlVisitor {
    fun start() {}
    fun end() {}
    fun attribute(name: String, value: String?) {
        attributeName(name)
        attributeValue(value)
    }

    fun attributeName(name: String) {}
    fun attributeValue(value: String?) {}
    fun value(body: String) {}
    fun cdata(body: String) {}
    fun subNode(name: String): SyncXmlVisitor
}
