package pw.binom.io

class ReaderUTF8(val stream: InputStream) : AbstractReader() {

    private val data = ByteArray(6)

    override fun close() {
        stream.close()
    }

    override fun read(): Char? =
            try {
                val firstByte = stream.read()
                val size = UTF8.utf8CharSize(firstByte)
                if (size > 0)
                    stream.read(data, 0, size)
                val vv = UTF8.utf8toUnicode(firstByte, data)
                vv
            } catch (e: EOFException) {
                null
            }
}

fun InputStream.utf8Reader() = ReaderUTF8(this)