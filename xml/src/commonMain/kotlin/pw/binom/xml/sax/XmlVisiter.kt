package pw.binom.xml.sax

interface XmlVisiter {
    suspend fun start()
    suspend fun end()
    suspend fun attribute(name: String, value: String?)
    suspend fun value(body: String)
    suspend fun cdata(body: String)
    suspend fun subNode(name: String): XmlVisiter
}