package pw.binom.xml.dom

interface NodeWriter {
    suspend fun node(tag: String, ns: String? = null, func: (suspend NodeBodyWriter.() -> Unit)? = null)
}