package pw.binom.compression.zlib

import pw.binom.io.OutputStream

class DeflaterOutputStream(val stream: OutputStream, level: Int, bufferSize: Int, wrap: Boolean) : OutputStream {

    private val deflater = Deflater(level, wrap)
    private val buffer = ByteArray(bufferSize)

    constructor(stream: OutputStream) : this(stream, 6, 512, false)

    private val cursor = Cursor()

    init {
        cursor.outputLength = buffer.size
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        cursor.inputLength = length
        cursor.inputOffset = offset

        while (true) {
            cursor.outputOffset = 0
            deflater.deflate(cursor, data, buffer)

            val writed = buffer.size - cursor.availOut
            if (writed > 0)
                stream.write(buffer, 0, writed)

            if (cursor.availOut > 0)
                break
        }
        return length
    }

    override fun flush() {
        while (true) {
            cursor.outputOffset = 0

            deflater.flush(cursor, buffer)


            val writed = buffer.size - cursor.availOut
            if (writed > 0)
                stream.write(buffer, 0, writed)
            if (cursor.availOut > 0)
                break
        }
    }

    override fun close() {
        deflater.close()
    }
}