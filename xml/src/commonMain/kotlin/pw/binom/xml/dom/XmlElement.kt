package pw.binom.xml.dom

import pw.binom.io.asAsync
import pw.binom.xml.sax.XmlVisiter
import pw.binom.xml.sax.XmlWriterVisiter

class XmlElement(var tag: String, var nameSpace: NameSpace?) {
    val attributes = HashMap<Attribute, String?>()
    val childs = ArrayList<XmlElement>()
    var body: String? = null

    suspend fun accept(visiter: XmlVisiter) {
        accept(HashMap(), visiter)
    }

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

        childs.forEach {
            it.nameSpace?.let { ns ->
                var p = prefix[ns.url]
                if (p == null) {
                    p = "ns${prefix.size + 1}"
                    visiter.attribute("xmlns:$p", ns.url)
                    prefix[ns.url] = p!!
                }
            }
        }

        childs.forEach {
            val key = it.nameSpace?.let { ns ->
                val p = prefix[ns.url]!!
                "$p:${it.tag}"

            } ?: it.tag

            it.accept(HashMap(prefix), visiter.subNode(key))
        }

        if (body != null)
            visiter.cdata(body!!)

        childs.forEach {

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
        accept(XmlWriterVisiter(tag, sb.asAsync()))
        return sb.toString()
    }
}

fun XmlElement.findElements(func: (XmlElement) -> Boolean) = childs.asSequence().filter(func)