package pw.binom.webdav.client

import pw.binom.date.DateTime
import pw.binom.date.parseRfc822Date
import pw.binom.webdav.DAV_NS
import pw.binom.xml.dom.XmlElement
import kotlin.jvm.JvmInline

@JvmInline
internal value class PropResponse(val element: XmlElement) {
    init {
        check(element.tag != "response" || element.nameSpace == DAV_NS) { "Invalid prop response. Excepted tag \"response\" with namespace \"DAV:\"" }
    }

    val href: String
        get() {
            val href = element.findOneDav("href") ?: throw IllegalStateException()
            return href.body!!
        }

    val props: Props
        get() {
            val prop = element.findOneDav("propstat")?.findOneDav("prop") ?: throw IllegalStateException()
            return Props(prop)
        }
}

@JvmInline
value class Props(val element: XmlElement) {
    val getLastModified: DateTime?
        get() {
            val getlastmodified =
                element.findOneDav("getlastmodified") ?: throw IllegalStateException()
            return getlastmodified.body?.parseRfc822Date()
        }

    val length: Long?
        get() =
            element.findOneDav("getcontentlength")?.body?.toULong()?.toLong()

    val isDirection: Boolean
        get() =
            element.findOneDav("resourcetype")?.findOneDav("collection") != null

    val quotaUsedBytes: Long?
        get() = element.findOneDav("quota-used-bytes")?.body?.toLongOrNull()

    val quotaAvailableBytes: Long?
        get() = element.findOneDav("quota-available-bytes")?.body?.toLongOrNull()

    val elements: List<XmlElement>
        get() = element.childs.filter { it.nameSpace == DAV_NS }
}

private fun XmlElement.findOneDav(name: String) =
    childs.find { it.nameSpace == DAV_NS && it.tag == name }
