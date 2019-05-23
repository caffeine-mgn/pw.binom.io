package pw.binom.io

class AsyncReaderUTF8(private val stream: AsyncInputStream) : AbstractAsyncReader() {
    override fun close() {
        stream.close()
    }

    override suspend fun read(): Char = stream.read().toChar()
}