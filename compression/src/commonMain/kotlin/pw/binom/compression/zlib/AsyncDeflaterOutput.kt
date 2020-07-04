package pw.binom.compression.zlib

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer

open class AsyncDeflaterOutput(
        val stream: AsyncOutput,
        level: Int = 6,
        bufferSize: Int = 512,
        wrap: Boolean = false,
        syncFlush: Boolean = true,
        val closeStream: Boolean = false
) : AsyncOutput {

    private val deflater = Deflater(level, wrap, syncFlush)
    private val buffer = ByteBuffer.alloc(bufferSize)
    protected val buf
        get() = buffer

    protected val def
        get() = deflater

    protected var usesDefaultDeflater = true

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
        if (closeStream) {
            stream.close()
        }
    }
}