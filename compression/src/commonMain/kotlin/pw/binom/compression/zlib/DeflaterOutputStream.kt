package pw.binom.compression.zlib

import pw.binom.io.OutputStream

open class DeflaterOutputStream(val stream: OutputStream, level: Int, bufferSize: Int = 512, wrap: Boolean = false, syncFlush: Boolean = true) : OutputStream {

    protected val deflater = Deflater(level, wrap, syncFlush)
    private val buffer = ByteArray(bufferSize)

    constructor(stream: OutputStream) : this(stream, 6, 512, false, true)

    protected val cursor = Cursor()

    protected var usesDefaultDeflater = true

    init {
        cursor.outputLength = buffer.size
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        cursor.inputLength = length
        cursor.inputOffset = offset

        while (true) {
            cursor.outputOffset = 0
            this.deflater.deflate(cursor, data, buffer)

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

            this.deflater.flush(cursor, buffer)


            val writed = buffer.size - cursor.availOut
            if (writed > 0)
                stream.write(buffer, 0, writed)
            if (cursor.availOut > 0)
                break
        }
        stream.flush()
    }

    protected open fun finish() {
        this.deflater.finish()
        flush()
        if (usesDefaultDeflater)
            this.deflater.end()
    }

    override fun close() {
        finish()
        this.deflater.close()
        stream.close()
    }
}