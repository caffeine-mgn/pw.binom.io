package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.tmp8

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

fun AsyncInput.utf8Reader() = AsyncReaderUTF82(this)