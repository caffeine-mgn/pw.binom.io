package pw.binom.compression.zlib

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.empty

//private val tmpBuf = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)

open class AsyncInflateInput(
        val stream: AsyncInput,
        bufferSize: Int = 512,
        wrap: Boolean = false,
        val closeStream: Boolean = false
) : AsyncInput {
    private val buf2 = ByteBuffer.alloc(bufferSize).empty()
    private val inflater = Inflater(wrap)
    protected var usesDefaultInflater = true

    protected suspend fun full() {
        if (buf2.remaining > 0)
            return
        buf2.clear()
        stream.read(buf2)
        buf2.flip()
    }

    override suspend fun read(dest: ByteBuffer): Int {
        val l = dest.remaining
        while (true) {
            full()
            if (buf2.remaining == 0 || dest.remaining == 0)
                break
            val r = inflater.inflate(buf2, dest)
            if (r == 0)
                break
        }
        return l - dest.remaining
    }

    override suspend fun close() {
        if (usesDefaultInflater)
            inflater.end()
        inflater.close()
        buf2.close()
        if (closeStream) {
            stream.close()
        }
    }

}