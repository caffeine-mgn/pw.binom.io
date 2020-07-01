package pw.binom.compression.zlib

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE

//private val tmpBuf = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)

open class AsyncInflateInput(val stream: AsyncInput, bufferSize: Int = 512, wrap: Boolean = false, val autoCloseStream: Boolean = false) : AsyncInput {
    private val buf2 = ByteBuffer.alloc(bufferSize)
    private val inflater = Inflater(wrap)
    protected var usesDefaultInflater = true
    private var first = true

//    override suspend fun skip(length: Long): Long {
//        var l = length
//        while (l > 0) {
//            tmpBuf.reset(0, minOf(tmpBuf.capacity, l.toInt()))
//            l -= readFully(tmpBuf)
//        }
//        return length
//    }

    protected suspend fun full() {
        if (!first && buf2.remaining > 0)
            return
        stream.read(buf2)
        first = false
    }

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        cursor.outputLength = length
//        cursor.outputOffset = offset
//        while (true) {
//            full()
//            if (cursor.availIn == 0 || cursor.availOut == 0)
//                break
//            val r = inflater.inflate(cursor, buf, data)
//            if (r == 0)
//                break
//        }
//        return length - cursor.outputLength
//    }

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
        if (autoCloseStream) {
            stream.close()
        }
    }

}