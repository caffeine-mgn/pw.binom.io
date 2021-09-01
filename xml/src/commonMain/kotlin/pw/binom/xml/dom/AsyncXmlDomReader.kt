package pw.binom.xml.dom

import pw.binom.io.AsyncReader
import pw.binom.xml.sax.AsyncXmlReaderVisitor
import pw.binom.xml.sax.AsyncXmlVisitor

class AsyncXmlDomReader private constructor(private val ctx: NameSpaceContext, tag: String) : AsyncXmlVisitor {
    class NameSpaceContext(var pool: HashMap<String, String> = HashMap()) {
        var default: String? = null
        private var autoIterator = 0

        /**
         * key - url
         * value - prefix
         */
        var prefix = HashMap<String, String>()


        fun pool(uri: String) = pool.getOrPut(uri) { "ns${autoIterator++}" }

        fun copy(): NameSpaceContext {
            val c = NameSpaceContext(pool)
            c.default = default
            c.prefix.putAll(prefix)
            return c
        }
    }

    constructor(tag: String) : this(ctx = NameSpaceContext(), tag = tag)
    constructor() : this(tag = "")

    val rootNode = XmlElement(
        tag = tag,
        nameSpace = null
    )

    override suspend fun attribute(name: String, value: String?) {
        if (name == "xmlns" && value != null) {
            ctx.default = ctx.pool(value)
            return
        }
        if (name.startsWith("xmlns:") && value != null) {
            ctx.prefix[name.removePrefix("xmlns:")] = ctx.pool(value)
            return
        }
        rootNode.attributes[Attribute(null, name)] = value
    }

    override suspend fun cdata(body: String) {
        rootNode.body = body
        rootNode.cdata = true
    }

    private fun fixCurrentNS() {
        if (rootNode.nameSpace == null) {
            if (":" !in rootNode.tag) {
                rootNode.nameSpace = ctx.default
            } else {
                val i = rootNode.tag.indexOf(":")
                val prefix = rootNode.tag.substring(0, i)
                val ns = ctx.prefix[prefix] ?: throw RuntimeException("Can't find prefix \"$prefix\"")
                rootNode.nameSpace = ns
                rootNode.tag = rootNode.tag.substring(i + 1)
            }
        }

        rootNode.attributes.forEach {
            if (it.key.nameSpace == null) {
                if (":" !in it.key.name) {
                    it.key.nameSpace = ctx.default
                } else {
                    val i = it.key.name.indexOf(":")
                    val prefix = it.key.name.substring(0, i)
                    val ns = ctx.prefix[prefix] ?: throw RuntimeException("Can't find prefix \"$prefix\"")
                    it.key.nameSpace = ns
                    it.key.name = it.key.name.substring(i + 1)
                }
            }
        }
    }

    override suspend fun end() {
        fixCurrentNS()
    }

    override suspend fun start() {
    }

    override suspend fun subNode(name: String): AsyncXmlVisitor {
        fixCurrentNS()
        val r = AsyncXmlDomReader(ctx.copy(), name)
        r.rootNode.parent = rootNode
        return r
    }

    override suspend fun value(body: String) {
        rootNode.body = body
    }
}

suspend fun AsyncReader.xmlTree(): XmlElement? {
    val r = AsyncXmlDomReader("")
    AsyncXmlReaderVisitor(this).accept(r)
    return r.rootNode.childs.getOrNull(0)
}