package pw.binom.xml.dom

import pw.binom.collections.defaultMutableMap
import pw.binom.io.AsyncAppendable
import pw.binom.xml.XML_NAMESPACE_PREFIX_WITH_DOTS
import pw.binom.xml.sax.AsyncXmlRootWriterVisitor
import pw.binom.xml.sax.AsyncXmlVisitor

private class TagWriteContext constructor(
    private val parent: TagWriteContext?,
    private val context: Context,
    private val writerVisitor: AsyncXmlVisitor
) : NodeBodyWriter {
    private val prefixMap = defaultMutableMap<String, String>()

    private fun prefix(uri: String): String? = prefixMap[uri] ?: parent?.prefix(uri)

    override suspend fun node(tag: String, ns: String?, func: (suspend NodeBodyWriter.() -> Unit)?) {
        if (ns != null) {
            var prefix = prefix(ns)
            if (prefix != null) {
                val w = writerVisitor.subNode("$prefix:$tag")
                w.start()

                val ctx = TagWriteContext(this, context, w)
                if (func != null) {
                    ctx.func()
                }
                w.end()
            } else {
                prefix = "ns${context.prefixCount++}"
                val w = writerVisitor.subNode("$prefix:$tag")
                w.start()

                val ctx = TagWriteContext(this, context, w)
                ctx.prefixMap[ns] = prefix
                w.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$prefix", ns)
                if (func != null) {
                    ctx.func()
                }
                w.end()
            }
        } else {
            val w = writerVisitor.subNode(tag)
            w.start()

            val ctx = TagWriteContext(this, context, w)
            if (func != null) {
                ctx.func()
            }
            w.end()
        }
    }

    override suspend fun attr(name: String, value: String, ns: String?) {
        if (ns != null) {
            var prefix = prefix(ns)
            if (prefix == null) {
                prefix = "ns${context.prefixCount++}"
                prefixMap[ns] = prefix
                writerVisitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$prefix", ns)
            }
            writerVisitor.attribute("$prefix:$name", value)
        } else {
            writerVisitor.attribute(name, value)
        }
    }

    override suspend fun value(text: String) {
        writerVisitor.value(text)
    }

    override suspend fun cdata(text: String) {
        writerVisitor.cdata(text)
    }
}

internal class Context {
    var prefixCount = 0
}

suspend fun AsyncAppendable.writeXml(headerCharset: String? = null, func: suspend NodeWriter.() -> Unit) {
    val w = if (headerCharset == null) {
        AsyncXmlRootWriterVisitor.withoutHeader(this)
    } else {
        AsyncXmlRootWriterVisitor.withHeader(this, headerCharset)
    }
    val ctx = TagWriteContext(null, Context(), w)
    w.start()
    func(ctx)
    w.end()
}
