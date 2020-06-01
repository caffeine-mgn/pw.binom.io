package pw.binom.compression.zlib

import pw.binom.io.AsyncOutputStream
import pw.binom.io.OutputStream

open class AsyncDeflaterOutputStream(val stream: AsyncOutputStream, level: Int, bufferSize: Int = 512, wrap: Boolean = false, syncFlush: Boolean = true) : AsyncOutputStream {

    private val deflater = Deflater(level, wrap, syncFlush)
    private val buffer = ByteArray(bufferSize)
    protected val buf
        get() = buffer

    protected val def
        get() = deflater

    constructor(stream: AsyncOutputStream) : this(stream, 6, 512, false, true)

    protected val cursor = Cursor()

    protected var usesDefaultDeflater = true

    init {
        cursor.outputLength = buffer.size
    }

    private val sync = ByteArray(1)

    override suspend fun write(data: Byte): Boolean {
        sync[0]=data
        return write(sync)==1
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
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

    override suspend fun flush() {
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

    protected open suspend fun finish() {
        deflater.finish()
        flush()
        if (usesDefaultDeflater)
            deflater.end()
    }

    override suspend fun close() {
        finish()
        deflater.close()
    }
}