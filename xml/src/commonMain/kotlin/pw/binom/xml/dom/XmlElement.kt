package pw.binom.xml.dom

import pw.binom.xml.sax.AsyncXmlVisitor
import pw.binom.xml.sax.SyncXmlVisitor
import pw.binom.xml.sax.SyncXmlWriterVisitor

class XmlElement(var tag: String, var nameSpace: String?) {
    constructor() : this(
        tag = "",
        nameSpace = null
    )

    val attributes = HashMap<Attribute, String?>()
    var cdata = false

    private val privateChilds = ArrayList<XmlElement>()
    val childs: List<XmlElement>
        get() = privateChilds
    var body: String? = null
    var parent: XmlElement? = null
        set(value) {
            field?.privateChilds?.remove(this)
            field = value
            field?.privateChilds?.add(this)
        }

    suspend fun accept(visitor: AsyncXmlVisitor) {
        accept(HashMap(), visitor)
    }

    fun accept(visitor: SyncXmlVisitor) {
        accept(HashMap(), visitor)
    }

    operator fun get(index: Int) = childs[index]

    private fun accept(prefix: HashMap<String, String>, visitor: SyncXmlVisitor) {
        visitor.start()
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visitor.attribute("xmlns:$p", ns)
                    prefix[ns] = p
                }
                "$p:${it.key.name}"

            } ?: it.key.name

            visitor.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!]
            visitor.attribute("xmlns:$p", nameSpace!!)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visitor.attribute("xmlns:$p", ns)
                    }
                    prefix[ns] = p!!
                }
            }
        }

        if (body != null) {
            if (cdata) {
                visitor.cdata(body!!)
            } else {
                visitor.value(body!!)
            }
        }

        privateChilds.forEach {

            val key = it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visitor.attribute("xmlns:$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.tag}"

            } ?: it.tag

            it.accept(HashMap(prefix), visitor.subNode(key))
        }
        visitor.end()
    }

    private suspend fun accept(prefix: HashMap<String, String>, visitor: AsyncXmlVisitor) {
        visitor.start()
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visitor.attribute("xmlns:$p", ns)
                    prefix[ns] = p
                }
                "$p:${it.key.name}"

            } ?: it.key.name

            visitor.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!]
            visitor.attribute("xmlns:$p", nameSpace!!)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visitor.attribute("xmlns:$p", ns)
                    }
                    prefix[ns] = p!!
                }
            }
        }

        if (body != null) {
            if (cdata) {
                visitor.cdata(body!!)
            } else {
                visitor.value(body!!)
            }
        }

        privateChilds.forEach {

            val key = it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visitor.attribute("xmlns:$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.tag}"

            } ?: it.tag

            it.accept(HashMap(prefix), visitor.subNode(key))
        }
        visitor.end()
    }

    fun asString(): String {
        val sb = StringBuilder()
        accept(SyncXmlWriterVisitor(tag, sb))
        return sb.toString()
    }
}

fun XmlElement.findElements(func: (XmlElement) -> Boolean) = childs.asSequence().filter(func)