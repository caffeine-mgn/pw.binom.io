package pw.binom.xml.dom

interface NodeBodyWriter : NodeWriter {
    suspend fun attr(name: String, value: String, ns: String? = null)
    suspend fun value(text: String)
    suspend fun cdata(text: String)
}