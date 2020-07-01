package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.tmp8

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

//    private val data = ByteDataBuffer.alloc(4)

    override suspend fun close() {
        stream.close()
    }

    override suspend fun read(): Char? =
            try {
                tmp8.reset(0, 1)
                if (stream.read(tmp8) == 0) {
                    null
                } else {
                    val firstByte = tmp8[0]
                    val size = UTF8.utf8CharSize(firstByte)
                    if (size > 0) {
                        tmp8.reset(0, size)
                        stream.read(tmp8)
                        tmp8.flip()
                    }
                    UTF8.utf8toUnicode(firstByte, tmp8)
                }
            } catch (e: EOFException) {
                null
            }
}

fun AsyncInputStream.utf8Reader() = AsyncReaderUTF8(this)
fun AsyncInput.utf8Reader() = AsyncReaderUTF82(this)