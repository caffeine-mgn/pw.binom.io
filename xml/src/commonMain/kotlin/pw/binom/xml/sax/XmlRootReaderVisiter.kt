package pw.binom.xml.sax

import pw.binom.io.AsyncComposeReader
import pw.binom.io.AsyncReader

class XmlRootReaderVisiter(reader: AsyncReader) {
    private val reader = AsyncComposeReader().addLast(reader)
    suspend fun accept(visiter: XmlVisiter) {
        reader.skipSpaces()
        if (!reader.readText("<?xml "))
            throw ExpectedException("<?xml ")
        while (true) {
            reader.skipSpaces()
            if (reader.readText("?>"))
                break
            reader.skipSpaces()
            reader.word()//property name
            reader.skipSpaces()
            if (!reader.readText("=")) {
                throw ExpectedException("=")
            }
            reader.readString()//property value
        }
        val root = XmlReaderVisiter(reader)
        root.accept(visiter)
    }
}