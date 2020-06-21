package pw.binom.compression.zlib

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer

open class AsyncDeflaterOutput(val stream: AsyncOutput, level: Int, bufferSize: Int = 512, wrap: Boolean = false, syncFlush: Boolean = true) : AsyncOutput {

    private val deflater = Deflater(level, wrap, syncFlush)
    private val buffer = ByteDataBuffer.alloc(bufferSize)
    protected val buf
        get() = buffer

    protected val def
        get() = deflater

    constructor(stream: AsyncOutput) : this(stream, 6, 512, false, true)

    protected val cursor = Cursor()

    protected var usesDefaultDeflater = true

    init {
        cursor.outputLength = buffer.size
    }

//    private val sync = ByteArray(1)
//
//    override suspend fun write(data: Byte): Boolean {
//        sync[0] = data
//        return write(sync) == 1
//    }

    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
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
            cursor.availIn = 0

            val r = deflater.flush(cursor, buffer)


            val writed = buffer.size - cursor.availOut
            if (writed > 0)
                stream.write(buffer, 0, writed)

            if (!r)
                break

//            if (cursor.availOut > 0)
//                break
        }
        stream.flush()
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
        stream.close()
    }
}