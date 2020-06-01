package pw.binom.io.http

import pw.binom.io.*

private const val CR = 0x0D.toByte()
private const val LF = 0x0A.toByte()

private suspend fun AsyncInputStream.readLineCRLF(): String {
    val sb = StringBuilder()
    while (true) {
        val r = read()
        if (r == CR) {
            if (read() != LF)
                throw IllegalStateException("Invalid end of line")
            return sb.toString()
        }

        if (r == 13.toByte()) {
            continue
        }
        sb.append(r.toChar())
    }
}

open class AsyncChunkedInputStream(val stream: AsyncInputStream) : AsyncHttpInputStream {
    override suspend fun read(): Byte {
        checkClosed()
        if (read(staticData) != 1)
            throw EOFException()
        return staticData[0]
    }

    private val staticData = ByteArray(1)
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
                val chunkedSize = stream.readLineCRLF()
                this.chunkedSize = chunkedSize.toULongOrNull(16)
                        ?: throw IOException("Invalid Chunk Size: \"$chunkedSize\"")
//                this.chunkedSize = this.chunkedSize!!
                readed = 0uL
            }

            if (chunkedSize == 0uL) {
                val b1 = stream.read()
                val b2 = stream.read()
                if (b1 != CR || b2 != LF)
                    throw IOException("Invalid end body  $b1  $b2")
                eof = true
                return 0
            }
            if (chunkedSize!! - readed == 0uL) {
                chunkedSize = null
                val b1 = stream.read()
                val b2 = stream.read()
                if (b1 != CR || b2 != LF)
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