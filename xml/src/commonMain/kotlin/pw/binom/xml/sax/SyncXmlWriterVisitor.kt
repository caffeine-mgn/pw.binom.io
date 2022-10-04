package pw.binom.xml.sax

class SyncXmlWriterVisitor(val nodeName: String, val appendable: Appendable) : SyncXmlVisitor {

    init {
        if ('<' in nodeName || '>' in nodeName) {
            throw IllegalArgumentException("Invalid node name \"$nodeName\"")
        }
    }

    private var progress = 0
    private var subnode = 0
    private var bodyStart = false
    private var started = false
    private var endded = false

    override fun start(tagName: String) {
        if (progress >= START) {
            throw IllegalStateException("Node already started")
        }
        appendable.append("<").append(tagName)
        started = true
        progress = START
    }

    override fun end() {
        if (progress >= END) {
            throw IllegalStateException("Node \"$nodeName\" already ended")
        }

        endded = true
        when (progress) {
            START -> appendable.append("/>")
            BODY -> appendable.append("</").append(nodeName).append(">")
        }
        progress = END
    }

    override fun attributeName(name: String) {
        if (progress < START) {
            throw IllegalStateException("Node not started")
        }

        if (progress > START) {
            throw IllegalStateException("Can't write attribute after body")
        }
        appendable.append(" ").append(name)
        super.attributeName(name)
    }

    override fun attributeValue(value: String?) {
        if (value != null) {
            appendable.append("=\"").append(value).append("\"")
        }
    }

    override fun value(body: String) {
        if (progress < START) {
            throw IllegalStateException("Node not started")
        }
        if (progress >= END) {
            throw IllegalStateException("Node \"$nodeName\" already closed")
        }
        if (progress == START) {
            progress = BODY
            appendable.append(">")
        }
        body.syncEncode(appendable)
    }

    override fun subNode(name: String): SyncXmlVisitor {
        if (progress < START) {
            throw IllegalStateException("Node not started")
        }
        if (progress >= END) {
            throw IllegalStateException("Node already closed")
        }
        if (progress == START) {
            progress = BODY
            appendable.append(">")
        }
        return SyncXmlWriterVisitor(name, appendable)
    }

    override fun cdata(body: String) {
        if (progress < START) {
            throw IllegalStateException("Node not started")
        }
        if (progress >= END) {
            throw IllegalStateException("Node already closed")
        }
        if (progress == START) {
            progress = BODY
            appendable.append(">")
        }
        appendable.append("<![CDATA[").append(body).append("]]>")
    }
}
