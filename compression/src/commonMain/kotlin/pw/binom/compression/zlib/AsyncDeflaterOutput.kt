package pw.binom.compression.zlib

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.StreamClosedException

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
    private var closed = false

    protected var usesDefaultDeflater = true
    private var busy = false
    private fun checkBusy() {
        if (busy) {
            throw IllegalStateException("Output is busy")
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        checkBusy()
        try {
            busy = true
            val vv = data.remaining
            while (true) {
                buffer.clear()
                val l = deflater.deflate(data, buffer)

                buffer.flip()
                while (buffer.remaining > 0) {
                    stream.write(buffer)
                }

                if (l <= 0)
                    break
            }
            return vv
        } finally {
            busy = false
        }
    }

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override suspend fun flush() {
        checkClosed()
        checkBusy()
        try {
            busy = true
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
        } finally {
            busy = false
        }
    }

    protected open suspend fun finish() {
        checkClosed()
        deflater.finish()
        flush()
        if (usesDefaultDeflater)
            deflater.end()
    }

    override suspend fun asyncClose() {
        checkClosed()
        checkBusy()
        finish()
        closed = true
        try {
            busy = true
            deflater.close()
            if (closeStream) {
                stream.asyncClose()
            }
        } finally {
            busy = false
        }
    }
}