package pw.binom.xml.dom

import pw.binom.io.AsyncReader
//import pw.binom.xml.sax.XmlRootReaderVisiter
import pw.binom.xml.sax.XmlVisiter

class XmlDomReader private constructor(private val ctx: NameSpaceContext, tag: String) : XmlVisiter {
    class NameSpaceContext(var pool: HashMap<String, NameSpace> = HashMap()) {
        var default: NameSpace? = null
        var prefix = HashMap<String, NameSpace>()


        fun pool(uri: String) = pool.getOrPut(uri) { NameSpace(uri) }

        fun copy(): NameSpaceContext {
            val c = NameSpaceContext(pool)
            c.default = default
            c.prefix.putAll(prefix)
            return c
        }
    }

    constructor(tag: String) : this(ctx = NameSpaceContext(), tag = tag)

    val node = XmlElement(tag = tag, nameSpace = null)

    override suspend fun attribute(name: String, value: String?) {
        if (name == "xmlns" && value != null) {
            ctx.default = ctx.pool(value)
            return
        }
        if (name.startsWith("xmlns:") && value != null) {
            ctx.prefix[name.removePrefix("xmlns:")] = ctx.pool(value)
            return
        }
        node.attributes[Attribute(null, name)] = value
    }

    override suspend fun cdata(body: String) {
        node.body = body
    }

    private fun fixCurrentNS() {
        if (node.nameSpace == null) {
            if (":" !in node.tag) {
                node.nameSpace = ctx.default
            } else {
                val i = node.tag.indexOf(":")
                val prefix = node.tag.substring(0, i)
                val ns = ctx.prefix[prefix] ?: throw RuntimeException("Can't find prefix \"$prefix\"")
                node.nameSpace = ns
                node.tag = node.tag.substring(i + 1)
            }
        }

        node.attributes.forEach {
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

    override suspend fun subNode(name: String): XmlVisiter {
        fixCurrentNS()
        val r = XmlDomReader(ctx.copy(), name)
        node.childs += r.node
        return r
    }

    override suspend fun value(body: String) {
        node.body = body
    }
}

suspend fun AsyncReader.xmlTree(): XmlElement? {
    val r = XmlDomReader("")
//    XmlRootReaderVisiter(this).accept(r)
    return r.node.childs.getOrNull(0)
}