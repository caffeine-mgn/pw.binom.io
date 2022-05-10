package pw.binom.compression.zlib

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.io.empty

// private val tmpBuf = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)

open class AsyncInflateInput(
    val stream: AsyncInput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    wrap: Boolean = false,
    val closeStream: Boolean = false
) : AsyncInput {
    private val buffer = ByteBuffer.alloc(bufferSize).empty()
    private val inflater = Inflater(wrap)
    protected var usesDefaultInflater = true
    private var eof = false
    private var busy = false

    private fun checkBusy() {
        if (busy) {
            throw IllegalStateException("Input is busy")
        }
    }

    private suspend fun full() {

//        if (eof) {
//            return
//        }
//        if (buffer.remaining == 0) {
//            buffer.clear()
//            if (stream.read(buffer) == 0) {
//                eof = true
//            }
//            buffer.flip()
//        }

        if (eof) {
            return
        }
        if (buffer.remaining123 > 0) {
            return
        }
        buffer.clear()
        while (buffer.remaining123 != 0) {
            val r = stream.read(buffer)
            if (r == 0) {
                eof = true
                break
            }
        }
        buffer.flip()
    }

    override val available: Int
        get() = -1

    override suspend fun read(dest: ByteBuffer): Int {
        checkBusy()
        try {
            busy = true
            val l = dest.remaining123
            while (true) {
                full()
                if (buffer.remaining123 == 0 || dest.remaining123 == 0) {
                    break
                }
                val r = inflater.inflate(buffer, dest)
                if (r == 0)
                    break
            }
            return l - dest.remaining123
        } finally {
            busy = false
        }
    }

    override suspend fun asyncClose() {
        checkBusy()
        try {
            busy = true
            if (usesDefaultInflater) {
                inflater.end()
            }
            runCatching { inflater.close() }
            runCatching { buffer.close() }
            if (closeStream) {
                stream.asyncClose()
            }
        } finally {
            busy = false
        }
    }
}
