package pw.binom.io.http

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.StreamClosedException
import pw.binom.skipAll

internal const val CR = 0x0D.toByte()
internal const val LF = 0x0A.toByte()

/**
 * Implements Async Http Chunked Transport Input
 *
 * @param stream real output stream
 * @param closeStream flag for auto close [stream] when this stream will close
 */
open class AsyncChunkedInput(val stream: AsyncInput, val closeStream: Boolean = false) : AsyncHttpInput {

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
            sb.append(r.toInt().toChar())
        }
    }

    private suspend fun AsyncInput.read(): Byte {
        staticData.reset(0, 1)
        readFully(staticData)
        return staticData[0]
    }

    private val staticData = ByteBuffer.alloc(2)
    override val isEof: Boolean
        get() = closed || eof

    override val available: Int
        get() = if (eof) 0 else -1

    private var chunkedSize: ULong? = null
    private var readed = 0uL
    private var eof = false
    private var closed = false

    private suspend fun readChankSize() {
        if (eof)
            return
        val chunkedSize = stream.readLineCRLF()
        if (chunkedSize.isEmpty()) {
            eof = true
            return
        }
        this.chunkedSize = chunkedSize.toULongOrNull(16)
            ?: throw IOException("Invalid Chunk Size: \"$chunkedSize\"")
        readed = 0uL

        if (this.chunkedSize!! == 0uL) {
            staticData.clear()
            stream.readFully(staticData)
            val b1 = staticData[0]
            val b2 = staticData[1]
            if (b1 != CR || b2 != LF)
                throw IOException("Invalid end body  $b1  $b2")
            eof = true
            return
        }
    }

    override suspend fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (eof) {
            return 0
        }
        while (true) {
            if (chunkedSize == null) {
                readChankSize()
            }

            if (eof)
                return 0

            if (chunkedSize!! - readed == 0uL) {
                chunkedSize = null
                staticData.clear()
                stream.readFully(staticData)
                val b1 = staticData[0]
                val b2 = staticData[1]
                if (b1 != CR || b2 != LF)
                    throw IOException("Invalid end of chunk")
                continue
            }

            val r = minOf(chunkedSize!! - readed, dest.remaining.toULong())
            val oldLimit = dest.limit
            dest.limit = dest.position + r.toInt()
            val b = stream.read(dest)
            dest.limit = oldLimit
            readed += b.toULong()
            if (chunkedSize!! - readed == 0uL) {
                staticData.clear()
                stream.readFully(staticData)
                val b1 = staticData[0]
                val b2 = staticData[1]
                if (b1 != CR || b2 != LF)
                    throw IOException("Invalid end of chunk")
                readChankSize()
            }
            return b
        }
    }

    override suspend fun asyncClose() {
        checkClosed()
        if (!eof) {
            skipAll()
        }
        closed = true
        staticData.close()
        if (closeStream) {
            stream.asyncClose()
        }
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

}