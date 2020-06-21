package pw.binom.compression.zlib

import pw.binom.ByteDataBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.AsyncInput

private val tmpBuf = ByteDataBuffer.alloc(DEFAULT_BUFFER_SIZE)

open class AsyncInflateInput(val stream: AsyncInput, bufferSize: Int = 512, wrap: Boolean = false) : AsyncInput {
    private val buf = ByteDataBuffer.alloc(bufferSize)
    private val inflater = Inflater(wrap)
    protected var usesDefaultInflater = true
    private var cursor = Cursor()
    private var first = true

    override suspend fun skip(length: Long): Long {
        var l = length
        while (l > 0) {
            l -= readFully(tmpBuf, 0, minOf(tmpBuf.size, l.toInt()))
        }
        return length
    }

    protected suspend fun full() {
        if (!first && cursor.availIn > 0)
            return

        cursor.inputOffset = 0
        cursor.inputLength = stream.read(buf, 0, buf.size)
        cursor.inputLength = maxOf(0, cursor.inputLength)
        first = false
    }

    private val static = ByteArray(1)

    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        cursor.outputLength = length
        cursor.outputOffset = offset
        while (true) {
            full()
            if (cursor.availIn == 0 || cursor.availOut == 0)
                break
            val r = inflater.inflate(cursor, buf, data)
            if (r==0)
                break
        }
        return length - cursor.outputLength
    }

    override suspend fun close() {
        if (usesDefaultInflater)
            inflater.end()
        inflater.close()
    }

}