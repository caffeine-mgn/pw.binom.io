package pw.binom.xml.sax

import pw.binom.io.AsyncAppendable

class SyncXmlRootWriterVisitor(val appendable: Appendable) : SyncXmlVisiter {
    private var started = false
    private var endded = false
    override fun start() {
        if (started)
            throw IllegalStateException("Root Node already started")
        started = true
        appendable.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    }

    override fun end() {
        if (!started)
            throw IllegalStateException("Root Node not started")
        if (endded)
            throw IllegalStateException("Root Node already closed")
        endded = true
    }

    override fun attribute(name: String, value: String?) {
        throw IllegalStateException("Root node not supports attributes")
    }

    override fun value(body: String) {
        if (body.isBlank())
            return
        throw IllegalStateException("Root node not supports attributes")
    }

    override fun cdata(body: String) {
        throw IllegalStateException("Root node not supports attributes")
    }

    override fun subNode(name: String): SyncXmlVisiter {
        if (!started)
            throw IllegalStateException("Root Node not started")
        if (endded)
            throw IllegalStateException("Root Node already closed")
        return SyncXmlWriterVisitor(name, appendable)
    }

}