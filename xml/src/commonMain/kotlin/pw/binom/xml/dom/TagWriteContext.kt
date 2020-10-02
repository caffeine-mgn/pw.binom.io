package pw.binom.xml.dom

import pw.binom.io.AsyncAppendable
import pw.binom.xml.sax.XmlRootWriterVisitor
import pw.binom.xml.sax.XmlVisiter

private class TagWriteContext constructor(
        private val parent: TagWriteContext?,
        private val context: Context,
        private val writerVisiter: XmlVisiter) : NodeBodyWriter {
    private val prefixMap = HashMap<String, String>()

    private fun prefix(uri: String): String? = prefixMap[uri] ?: parent?.prefix(uri)

    override suspend fun node(tag: String, ns: String?, func: (suspend NodeBodyWriter.() -> Unit)?) {
        if (ns != null) {
            var prefix = prefix(ns)
            if (prefix != null) {
                val w = writerVisiter.subNode("$prefix:$tag")
                w.start()

                val ctx = TagWriteContext(this, context, w)
                if (func != null)
                    ctx.func()
                w.end()
            } else {
                prefix = "ns${context.prefixCount++}"
                val w = writerVisiter.subNode("$prefix:$tag")
                w.start()

                val ctx = TagWriteContext(this, context, w)
                ctx.prefixMap[ns] = prefix
                w.attribute("xmlns:$prefix", ns)
                if (func != null)
                    ctx.func()
                w.end()
            }
        } else {
            val w = writerVisiter.subNode(tag)
            w.start()

            val ctx = TagWriteContext(this, context, w)
            if (func != null)
                ctx.func()
            w.end()
        }
    }

    override suspend fun attr(name: String, value: String, ns: String?) {
        if (ns != null) {
            var prefix = prefix(ns)
            if (prefix == null) {
                prefix = "ns${context.prefixCount++}"
                prefixMap[ns] = prefix
                writerVisiter.attribute("xmlns:$prefix", ns)
            }
            writerVisiter.attribute("$prefix:$name", value)
        } else {
            writerVisiter.attribute(name, value)
        }

    }

    override suspend fun value(text: String) {
        writerVisiter.value(text)
    }

    override suspend fun cdata(text: String) {
        writerVisiter.cdata(text)
    }
}

internal class Context {
    var prefixCount = 0
}

suspend fun xml(appendable: AsyncAppendable, func: suspend NodeWriter.() -> Unit) {
    val w = XmlRootWriterVisitor(appendable)
    val ctx = TagWriteContext(null, Context(), w)
    w.start()
    func(ctx)
    w.end()
}