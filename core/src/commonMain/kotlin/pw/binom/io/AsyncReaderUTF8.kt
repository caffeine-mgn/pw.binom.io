package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput

@Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
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

class AsyncReaderUTF82(private val stream: AsyncInput) : AbstractAsyncReader() {

    private val data = ByteDataBuffer.alloc(4)

    override suspend fun close() {
        stream.close()
    }

    override suspend fun read(): Char? =
            try {
                if (stream.read(data, length = 1) == 0) {
                    null
                } else {
                    val firstByte = data[0]
                    val size = UTF8.utf8CharSize(firstByte)
                    if (size > 0)
                        stream.read(data, length = size)
                    UTF8.utf8toUnicode(firstByte, data)
                }
            } catch (e: EOFException) {
                null
            }
}

fun AsyncInputStream.utf8Reader() = AsyncReaderUTF8(this)
fun AsyncInput.utf8Reader() = AsyncReaderUTF82(this)