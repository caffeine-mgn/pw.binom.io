package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.io.StreamClosedException

open class DeflaterOutput(
    val stream: Output,
    level: Int = 6,
    bufferSize: Int = 1024,
    wrap: Boolean = false,
    syncFlush: Boolean = true,
    val closeStream: Boolean = false
) : Output {

    private val deflater = Deflater(level, wrap, syncFlush)
    private val buffer = ByteBuffer.alloc(bufferSize)
    protected val buf
        get() = buffer

    protected val def
        get() = deflater

    protected var usesDefaultDeflater = true
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

//    private val sync = ByteArray(1)
//
//    override suspend fun write(data: Byte): Boolean {
//        sync[0] = data
//        return write(sync) == 1
//    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        val vv = data.remaining
        while (data.remaining > 0) {
            buffer.clear()
            val l = deflater.deflate(data, buffer)

            buffer.flip()
            stream.write(buffer)

            if (l <= 0)
                break
        }
        return vv
    }

    override fun flush() {
        checkClosed()
        while (true) {
            buffer.clear()
            val r = deflater.flush(buffer)
            buffer.flip()
            if (buffer.remaining > 0) {
                val pp = stream.write(buffer)

            }
            if (!r)
                break
        }
        stream.flush()
    }

    protected open fun finish() {
        checkClosed()
        deflater.finish()
        flush()
        if (usesDefaultDeflater)
            deflater.end()
    }

    override fun close() {
        flush()
        finish()
        closed = true
        deflater.close()
        if (closeStream) {
            stream.close()
        }
    }
}