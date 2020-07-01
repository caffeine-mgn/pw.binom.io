package pw.binom.compression.zlib

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer

open class AsyncDeflaterOutput(
        val stream: AsyncOutput,
        level: Int = 6,
        bufferSize: Int = 512,
        wrap: Boolean = false,
        syncFlush: Boolean = true,
        val autoCloseStream: Boolean = false
) : AsyncOutput {

    private val deflater = Deflater(level, wrap, syncFlush)
    private val buffer = ByteBuffer.alloc(bufferSize)
    protected val buf
        get() = buffer

    protected val def
        get() = deflater

//    protected val cursor = Cursor()

    protected var usesDefaultDeflater = true

//    init {
//        cursor.outputLength = buffer.size
//    }

//    private val sync = ByteArray(1)
//
//    override suspend fun write(data: Byte): Boolean {
//        sync[0] = data
//        return write(sync) == 1
//    }

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        cursor.inputLength = length
//        cursor.inputOffset = offset
//
//        while (true) {
//            cursor.outputOffset = 0
//            deflater.deflate(cursor, data, buffer)
//
//            val writed = buffer.size - cursor.availOut
//            if (writed > 0)
//                stream.write(buffer, 0, writed)
//
//            if (cursor.availOut > 0)
//                break
//        }
//        return length
//    }

    override suspend fun write(data: ByteBuffer): Int {
        val vv = data.remaining
        while (true) {
            buffer.clear()
            val l = deflater.deflate(data, buffer)

            buffer.flip()
            stream.write(buffer)

            if (l <= 0)
                break
        }
        return vv
    }

    override suspend fun flush() {
        while (true) {
            buffer.clear()
            val r = deflater.flush(buffer)
            val writed = buffer.position
            buffer.flip()
            if (writed > 0)
                stream.write(buffer)
            if (!r)
                break
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
        if (autoCloseStream) {
            stream.close()
        }
    }
}