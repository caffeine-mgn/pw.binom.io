package pw.binom.io

import pw.binom.Input
import pw.binom.tmp8

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

fun Input.utf8Reader() = ReaderUTF82(this)