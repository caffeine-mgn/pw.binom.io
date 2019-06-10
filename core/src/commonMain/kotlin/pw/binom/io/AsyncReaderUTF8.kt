package pw.binom.io

class AsyncReaderUTF8(private val stream: AsyncInputStream) : AbstractAsyncReader() {
    override suspend fun close() {
        stream.close()
    }

    override suspend fun read(): Char = UTF8.read(stream)
}

fun AsyncInputStream.utf8Reader() = AsyncReaderUTF8(this)