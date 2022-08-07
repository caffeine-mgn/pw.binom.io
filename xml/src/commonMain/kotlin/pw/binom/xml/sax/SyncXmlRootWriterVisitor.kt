package pw.binom.xml.sax

class SyncXmlRootWriterVisitor(val appendable: Appendable) : SyncXmlVisitor {
    private var started = false
    private var endded = false
    override fun start() {
        if (started) {
            throw IllegalStateException("Root Node already started")
        }
        started = true
        appendable.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    }

    override fun end() {
        if (!started) {
            throw IllegalStateException("Root Node not started")
        }
        if (endded) {
            throw IllegalStateException("Root Node already closed")
        }
        endded = true
    }

    override fun attribute(name: String, value: String?) {
        throw IllegalStateException("Can't write attribute \"$name\" with value \"$value\": Root node not supports attributes")
    }

    override fun value(body: String) {
        if (body.isBlank()) {
            return
        }
        throw IllegalStateException("Can't write value \"$body\": Root node not supports body")
    }

    override fun cdata(body: String) {
        throw IllegalStateException("Root node not supports attributes")
    }

    override fun subNode(name: String): SyncXmlVisitor {
        if (!started) {
            throw IllegalStateException("Root Node not started")
        }
        if (endded) {
            throw IllegalStateException("Root Node already closed")
        }
        return SyncXmlWriterVisitor(name, appendable)
    }
}
