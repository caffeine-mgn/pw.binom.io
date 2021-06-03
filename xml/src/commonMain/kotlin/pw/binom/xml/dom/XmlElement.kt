package pw.binom.xml.dom

import pw.binom.io.asAsync
import pw.binom.xml.sax.AsyncXmlVisiter
import pw.binom.xml.sax.AsyncXmlWriterVisitor
import pw.binom.xml.sax.SyncXmlVisiter
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

    suspend fun accept(visiter: AsyncXmlVisiter) {
        accept(HashMap(), visiter)
    }

    fun accept(visiter: SyncXmlVisiter) {
        accept(HashMap(), visiter)
    }

    operator fun get(index: Int) = childs[index]

    private fun accept(prefix: HashMap<String, String>, visiter: SyncXmlVisiter) {
        visiter.start()
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns)
                    prefix[ns] = p
                }
                "$p:${it.key.name}"

            } ?: it.key.name

            visiter.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!]
            visiter.attribute("xmlns:$p", nameSpace!!)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visiter.attribute("xmlns:$p", ns)
                    }
                    prefix[ns] = p!!
                }
            }
        }

        if (body != null) {
            if (cdata) {
                visiter.cdata(body!!)
            } else {
                visiter.value(body!!)
            }
        }

        privateChilds.forEach {

            val key = it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.tag}"

            } ?: it.tag

            it.accept(HashMap(prefix), visiter.subNode(key))
        }
        visiter.end()
    }

    private suspend fun accept(prefix: HashMap<String, String>, visiter: AsyncXmlVisiter) {
        visiter.start()
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns)
                    prefix[ns] = p
                }
                "$p:${it.key.name}"

            } ?: it.key.name

            visiter.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!]
            visiter.attribute("xmlns:$p", nameSpace!!)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visiter.attribute("xmlns:$p", ns)
                    }
                    prefix[ns] = p!!
                }
            }
        }

        if (body != null) {
            if (cdata) {
                visiter.cdata(body!!)
            } else {
                visiter.value(body!!)
            }
        }

        privateChilds.forEach {

            val key = it.nameSpace?.let { ns ->
                var p = prefix[ns]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns)
                    prefix[ns] = p!!
                }
                "$p:${it.tag}"

            } ?: it.tag

            it.accept(HashMap(prefix), visiter.subNode(key))
        }
        visiter.end()
    }

    fun asString(): String {
        val sb = StringBuilder()
        accept(SyncXmlWriterVisitor(tag, sb))
        return sb.toString()
    }
}

fun XmlElement.findElements(func: (XmlElement) -> Boolean) = childs.asSequence().filter(func)