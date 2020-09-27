package pw.binom.xml.dom

import pw.binom.io.asAsync
import pw.binom.xml.sax.XmlVisiter
import pw.binom.xml.sax.XmlWriterVisitor

class XmlElement(var tag: String, var nameSpace: NameSpace?) {
    constructor() : this(
            tag = "",
            nameSpace = null
    )

    val attributes = HashMap<Attribute, String?>()

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

    suspend fun accept(visiter: XmlVisiter) {
        accept(HashMap(), visiter)
    }

    operator fun get(index: Int) = childs[index]

    private suspend fun accept(prefix: HashMap<String, String>, visiter: XmlVisiter) {
        visiter.start()
        attributes.forEach {
            val key = it.key.nameSpace?.let { ns ->
                var p = prefix[ns.url]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns.url)
                    prefix[ns.url] = p
                }
                "$p:${it.key.name}"

            } ?: it.key.name

            visiter.attribute(key, it.value)
        }

        if (nameSpace != null && parent?.tag?.isEmpty() == true) {
            val p = prefix[nameSpace!!.url]
            visiter.attribute("xmlns:$p", nameSpace!!.url)
        }

        privateChilds.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns.url]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    if (tag.isNotEmpty()) {
                        visiter.attribute("xmlns:$p", ns.url)
                    }
                    prefix[ns.url] = p!!
                }
            }
        }

//        childs.forEach {
//            val key = it.nameSpace?.let { ns ->
//                val p = prefix[ns.url]!!
//                "$p:${it.tag}"
//
//            } ?: it.tag
//
//            it.accept(HashMap(prefix), visiter.subNode(key))
//        }

        if (body != null)
            visiter.value(body!!)

        privateChilds.forEach {

            val key = it.nameSpace?.let { ns ->
                var p = prefix[ns.url]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns.url)
                    prefix[ns.url] = p!!
                }
                "$p:${it.tag}"

            } ?: it.tag

            it.accept(HashMap(prefix), visiter.subNode(key))
        }
        visiter.end()
    }

    suspend fun asString(): String {
        val sb = StringBuilder()
        accept(XmlWriterVisitor(tag, sb.asAsync()))
        return sb.toString()
    }
}

fun XmlElement.findElements(func: (XmlElement) -> Boolean) = childs.asSequence().filter(func)