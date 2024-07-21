package pw.binom.io

class ReaderUTF82(val stream: Input) : Reader {

    private val data = ByteBuffer(4)

    override fun close() {
        data.close()
        stream.close()
    }

    override fun read(): Char? =
        try {
            data.reset(0, 1)
            if (stream.read(data).isAvailable) {
                val firstByte = data[0]
                val size = UTF8.getUtf8CharSize(firstByte) - 1
                if (size > 0) {
                    data.reset(0, size)
                    stream.read(data)
                    data.flip()
                }
                UTF8.utf8toUnicode(firstByte, data)
            } else {
                null
            }
        } catch (e: EOFException) {
            null
        }
}

fun Input.utf8Reader() = ReaderUTF82(this)
