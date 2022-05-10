package pw.binom.io

class AsyncReaderUTF82(private val stream: AsyncInput) : AbstractAsyncReader() {

    private val data = ByteBuffer.alloc(4)

    override suspend fun asyncClose() {
        data.close()
        stream.asyncClose()
    }

    override suspend fun readChar(): Char? =
        try {
            data.reset(0, 1)
            if (stream.read(data) == 0) {
                null
            } else {
                val firstByte = data[0]
                val size = UTF8.getUtf8CharSize(firstByte) - 1
                if (size > 0) {
                    data.reset(0, size)
                    stream.read(data)
                    data.flip()
                }
                UTF8.utf8toUnicode(firstByte, data)
            }
        } catch (e: EOFException) {
            null
        }
}

fun AsyncInput.utf8Reader() = AsyncReaderUTF82(this)
