package pw.binom.xml.dom

import pw.binom.collections.defaultMutableMap
import pw.binom.xml.XML_NAMESPACE_PREFIX_WITH_DOTS
import pw.binom.xml.sax.AsyncXmlVisitor
import pw.binom.xml.sax.SyncXmlVisitor
import pw.binom.xml.sax.SyncXmlWriterVisitor

class XmlElement(var tag: String, var nameSpace: String?) {
    constructor() : this(
        tag = "",
        nameSpace = null
    )

    val attributes = defaultMutableMap<Attribute, String?>()
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
        accept(defaultMutableMap(), visitor)
    }

    fun accept(visitor: SyncXmlVisitor) {
        accept(defaultMutableMap(), visitor)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("XmlElement(tag=$tag")
        if (nameSpace != null) {
            sb.append(", NameSpace=$nameSpace")
        }
        sb.append(")")
        return sb.toString()
    }

    operator fun get(index: Int) = childs[index]

    private fun accept(prefix: MutableMap<String, String>, visitor: SyncXmlVisitor) {
        var tagName = tag
        val nameSpace = nameSpace
        if (nameSpace != null) {
            var p = prefix[nameSpace]
            if (p == null) {
                p = "ns${prefix.size + 1}"
                tagName = "$p:$tagName"
                visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", nameSpace)
                prefix[nameSpace] = p
            }
        }
        visitor.start(tagName)
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.key.name}"
            } ?: it.key.name

            visitor.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!]
            visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", nameSpace!!)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", ns)
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
                    visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.tag}"
            } ?: it.tag

            it.accept(defaultMutableMap(prefix), visitor.subNode(key))
        }
        visitor.end()
    }

    private suspend fun accept(prefix: MutableMap<String, String>, visitor: AsyncXmlVisitor) {
        var tagName = tag
        val nameSpace = nameSpace
        var started = false
        if (nameSpace != null) {
            var p = prefix[nameSpace]
            if (p == null) {
                p = "ns${prefix.size + 1}"
                tagName = "$p:$tagName"
                visitor.start()
                started = true
                visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", nameSpace)
                prefix[nameSpace] = p
            }
        }
        if (!started) {
            visitor.start()
        }
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.key.name}"
            } ?: it.key.name

            visitor.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!]
            visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", nameSpace!!)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", ns)
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
                    visitor.attribute("$XML_NAMESPACE_PREFIX_WITH_DOTS$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.tag}"
            } ?: it.tag

            it.accept(defaultMutableMap(prefix), visitor.subNode(key))
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
