package pw.binom.io.http

import pw.binom.io.*

open class AsyncChunkedInputStream(val stream: AsyncInputStream) : AsyncHttpInputStream {
    override val isEof: Boolean
        get() = closed || eof
    private var chunkedSize: ULong? = null
    private var readed = 0uL
    private var eof = false
    private var closed = false

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        if (eof)
            return 0
        while (true) {
            if (chunkedSize == null) {
                val chunkedSize = stream.readln()
                this.chunkedSize = chunkedSize.toULongOrNull(16)
                        ?: throw IOException("Invalid Chunk Size: \"$chunkedSize\"")
                this.chunkedSize = this.chunkedSize!!
                readed = 0uL
            }

            if (chunkedSize == 0uL) {
                val b1=stream.read()
                val b2=stream.read()
                if (
                        b1 != 13.toByte()
                        || b2 != 10.toByte()
                )
                    throw IOException("Invalid end body  $b1  $b2")
                eof = true
                close()
                return 0
            }
            if (chunkedSize!! - readed <= 0uL) {
                chunkedSize = null
                if (
                        stream.read() != 13.toByte()
                        || stream.read() != 10.toByte()
                )
                    throw IOException("Invalid end of chunk")
                continue
            }

            val r = minOf(chunkedSize!! - readed, length.toULong())
            val b = stream.read(data, offset, r.toInt())
            readed += b.toULong()
            return b
        }
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

}