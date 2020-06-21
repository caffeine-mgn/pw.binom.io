package pw.binom.io.http

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput
import pw.binom.io.*

private const val CR = 0x0D.toByte()
private const val LF = 0x0A.toByte()

open class AsyncChunkedInput(val stream: AsyncInput) : AsyncHttpInput {

    private suspend fun AsyncInput.readLineCRLF(): String {
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

    private suspend fun AsyncInput.read():Byte{
        readFully(staticData,length = 1)
        return staticData[0]
    }

    private val staticData = ByteDataBuffer.alloc(2)
    override val isEof: Boolean
        get() = closed || eof

    override suspend fun skip(length: Long): Long {
        TODO("Not yet implemented")
    }

    private var chunkedSize: ULong? = null
    private var readed = 0uL
    private var eof = false
    private var closed = false

    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
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
                stream.readFully(staticData)
                val b1 = staticData[0]
                val b2 = staticData[1]
                if (b1 != CR || b2 != LF)
                    throw IOException("Invalid end body  $b1  $b2")
                eof = true
                return 0
            }
            if (chunkedSize!! - readed == 0uL) {
                chunkedSize = null
                stream.readFully(staticData)
                val b1 = staticData[0]
                val b2 = staticData[1]
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