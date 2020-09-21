package pw.binom.xml.sax

import pw.binom.io.AsyncAppendable

class XmlRootWriterVisiter(val appendable: AsyncAppendable) : XmlVisiter {
    private var started = false
    private var endded = false
    override suspend fun start() {
        if (started)
            throw IllegalStateException("Root Node already started")
        started = true
        appendable.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    }

    override suspend fun end() {
        if (!started)
            throw IllegalStateException("Root Node not started")
        if (endded)
            throw IllegalStateException("Root Node already closed")
        endded = true
    }

    override suspend fun attribute(name: String, value: String?) {
        throw IllegalStateException("Root node not supports attributes")
    }

    override suspend fun value(body: String) {
        if (body.isBlank())
            return
        throw IllegalStateException("Root node not supports attributes")
    }

    override suspend fun cdata(body: String) {
        throw IllegalStateException("Root node not supports attributes")
    }

    override suspend fun subNode(name: String): XmlVisiter {
        if (!started)
            throw IllegalStateException("Root Node not started")
        if (endded)
            throw IllegalStateException("Root Node already closed")
        return XmlWriterVisiter(name, appendable)
    }

}