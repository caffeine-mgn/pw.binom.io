package pw.binom.io

class AsyncReaderUTF8(private val stream: AsyncInputStream) : AbstractAsyncReader() {

    private val data = ByteArray(6)

    override suspend fun close() {
        stream.close()
    }

    override suspend fun read(): Char? =
            try {
                val firstByte = stream.read()
                val size = UTF8.utf8CharSize(firstByte)
                if (size > 0)
                    stream.read(data, 0, size)
                UTF8.utf8toUnicode(firstByte, data)
            } catch (e: EOFException) {
                null
            }
}

fun AsyncInputStream.utf8Reader() = AsyncReaderUTF8(this)