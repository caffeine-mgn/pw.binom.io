package pw.binom.io

class ReaderUTF8(val stream: InputStream) : AbstractReader() {
    override fun close() {
        stream.close()
    }

    override fun read(): Char? = UTF8.read(stream)

}

fun InputStream.utf8Reader() = ReaderUTF8(this)