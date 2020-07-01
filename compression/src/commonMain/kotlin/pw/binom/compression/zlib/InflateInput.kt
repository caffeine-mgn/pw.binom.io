package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.Input

//private val tmpBuf = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)

open class InflateInput(val stream: Input, bufferSize: Int = 512, wrap: Boolean = false, val autoCloseStream: Boolean = false) : Input {
    private val buf2 = ByteBuffer.alloc(bufferSize)
    private val inflater = Inflater(wrap)
    protected var usesDefaultInflater = true
    private var first = true

//    override fun skip(length: Long): Long {
//        var l = length
//        while (l > 0) {
//            tmpBuf.reset(0, minOf(tmpBuf.capacity, l.toInt()))
//            l -= read(tmpBuf)
//        }
//        return length
//    }

    override fun read(dest: ByteBuffer): Int {
        val l = dest.remaining
        while (true) {
            full2()
            if (buf2.remaining == 0 || dest.remaining == 0)
                break
            println("inflate! ${buf2.remaining}")
            val r = inflater.inflate(buf2, dest)
            if (r == 0)
                break
        }
        return l - dest.remaining
    }

    protected fun full2() {
        if (!first && buf2.remaining > 0)
            return
        stream.read(buf2)
        buf2.flip()
        first = false
    }

//    protected fun full() {
//        if (!first && cursor.availIn > 0)
//            return
//
//        cursor.inputOffset = 0
//        cursor.inputLength = stream.read(buf, 0, buf.size)
//        cursor.inputLength = maxOf(0, cursor.inputLength)
//        first = false
//    }
//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
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

    override fun close() {
        if (usesDefaultInflater)
            inflater.end()
        inflater.close()
        buf2.close()
        if (autoCloseStream) {
            stream.close()
        }
    }

}