package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Input

@Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
class ReaderUTF8(val stream: InputStream) : AbstractReader() {

    private val data = ByteArray(4)

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

class ReaderUTF82(val stream: Input) : AbstractReader() {

    private val data = ByteDataBuffer.alloc(4)

    override fun close() {
        stream.close()
    }

    override fun read(): Char? =
            try {
                stream.read(data, length = 1)
                val firstByte = data[0]
                val size = UTF8.utf8CharSize(firstByte)
                if (size > 0)
                    stream.read(data, length = size)
                UTF8.utf8toUnicode(firstByte, data)
            } catch (e: EOFException) {
                null
            }
}

fun InputStream.utf8Reader() = ReaderUTF8(this)