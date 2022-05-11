package pw.binom.xml.sax

interface AsyncXmlVisitor {
    suspend fun start() {}
    suspend fun end() {}
    suspend fun attribute(name: String, value: String?) {
        attributeName(name)
        attributeValue(value)
    }

    suspend fun attributeName(name: String) {}
    suspend fun attributeValue(value: String?) {}
    suspend fun value(body: String) {}
    suspend fun cdata(body: String) {}
    suspend fun subNode(name: String): AsyncXmlVisitor = this
}
