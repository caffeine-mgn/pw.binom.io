package pw.binom.xml.dom

class Attribute(var nameSpace: NameSpace?, var name: String) {
    override fun toString(): String {
        val nameSpace = nameSpace
        return if (nameSpace == null) {
            "Attribute(name: [$name])"
        } else {
            "Attribute(nameSpace: [${nameSpace.url}], name: [$name])"
        }
    }
}