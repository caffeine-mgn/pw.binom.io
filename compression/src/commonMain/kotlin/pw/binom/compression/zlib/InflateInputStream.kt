package pw.binom.compression.zlib

import pw.binom.io.InputStream

private val tmpBuf = ByteArray(32)

open class InflateInputStream(val stream: InputStream, bufferSize: Int = 512, wrap: Boolean = false) : InputStream {
    private val buf = ByteArray(bufferSize)
    private val inflater = Inflater(wrap)
    protected var usesDefaultInflater = true

    private var cursor = Cursor()
    private var first = true

    override fun skip(length: Long): Long {
        var l = length
        while (l > 0) {
            l -= read(tmpBuf, 0, minOf(tmpBuf.size, l.toInt()))
        }
        return length
    }

    protected fun full() {
        if (!first && cursor.availIn > 0)
            return

        cursor.inputOffset = 0
        cursor.inputLength = stream.read(buf, 0, buf.size)
        cursor.inputLength = maxOf(0, cursor.inputLength)
        first = false
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        cursor.outputLength = length
        cursor.outputOffset = offset
        while (true) {
            full()
            if (cursor.availIn == 0 || cursor.availOut == 0)
                break
            val b = inflater.inflate(cursor, buf, data)
            if (b==0)
                break
        }
        return length - cursor.availOut
    }

    override fun close() {
        if (usesDefaultInflater)
            inflater.end()
        inflater.close()
    }

}