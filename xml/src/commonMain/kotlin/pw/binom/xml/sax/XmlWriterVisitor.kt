package pw.binom.xml.sax

import pw.binom.io.AsyncAppendable

private const val START = 1
private const val BODY = 2
private const val END = 3

private suspend fun String.encode(appendable: AsyncAppendable){
    forEach {
        when (it){
            '<'->appendable.append("&lt;")
            '>'->appendable.append("&gt;")
            '&'->appendable.append("&#38;")
            '\''->appendable.append("&#39;")
            '"'->appendable.append("&#34;")
            else->appendable.append(it)
        }
    }
}

class XmlWriterVisitor(val nodeName: String, val appendable: AsyncAppendable) : XmlVisiter {

    init {
        if ('<' in nodeName || '>' in nodeName)
            throw IllegalArgumentException("Invalid node name \"$nodeName\"")
    }

    private var progress = 0
    private var subnode = 0
    private var bodyStart = false
    private var started = false
    private var endded = false

    override suspend fun start() {
        if (progress >= START)
            throw IllegalStateException("Node already started")
        appendable.append("<").append(nodeName)
        started = true
        progress = START
    }

    override suspend fun end() {

        if (progress >= END)
            throw IllegalStateException("Node \"$nodeName\" already ended")

        endded = true
        when (progress) {
            START -> appendable.append("/>")
            BODY -> appendable.append("</").append(nodeName).append(">")
        }
        progress = END
    }

    override suspend fun attributeName(name: String) {
        if (progress < START)
            throw IllegalStateException("Node not started")

        if (progress > START)
            throw IllegalStateException("Can't write attribute after body")
        appendable.append(" ").append(name)
        super.attributeName(name)
    }

    override suspend fun attributeValue(value: String?) {
        if (value != null) {
            appendable.append("=\"").append(value).append("\"")
        }
    }

    override suspend fun value(body: String) {
        if (progress < START)
            throw IllegalStateException("Node not started")
        if (progress >= END)
            throw IllegalStateException("Node \"$nodeName\" already closed")
        if (progress == START) {
            progress = BODY
            appendable.append(">")
        }
        body.encode(appendable)
    }

    override suspend fun subNode(name: String): XmlVisiter {
        if (progress < START)
            throw IllegalStateException("Node not started")
        if (progress >= END)
            throw IllegalStateException("Node already closed")
        if (progress == START) {
            progress = BODY
            appendable.append(">")
        }
        return XmlWriterVisitor(name, appendable)
    }

    override suspend fun cdata(body: String) {
        if (progress < START)
            throw IllegalStateException("Node not started")
        if (progress >= END)
            throw IllegalStateException("Node already closed")
        if (progress == START) {
            progress = BODY
            appendable.append(">")
        }
        appendable.append("<![CDATA[").append(body).append("]]>")
    }
}