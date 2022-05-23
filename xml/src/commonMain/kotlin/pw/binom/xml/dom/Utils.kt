package pw.binom.xml.dom

fun Map<Attribute, String?>.find(name: String, nameSpace: String?): String? {
    val attr = keys.find { it.name == name && it.nameSpace == nameSpace } ?: return null
    return this[attr]
}
