package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Input
import pw.binom.tmp8

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

//    private val data = ByteDataBuffer.alloc(4)

    override fun close() {
        stream.close()
    }

    override fun read(): Char? =
            try {
                tmp8.reset(0,1)
                stream.read(tmp8)
                val firstByte = tmp8[0]
                val size = UTF8.utf8CharSize(firstByte)
                if (size > 0) {
                    tmp8.clear()
                    stream.read(tmp8)
                    tmp8.flip()
                }
                UTF8.utf8toUnicode(firstByte, tmp8)
            } catch (e: EOFException) {
                null
            }
}

fun InputStream.utf8Reader() = ReaderUTF8(this)