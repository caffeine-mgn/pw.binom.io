package pw.binom.db.radis

import pw.binom.CharBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.*

class AsyncBufferedReaderInput(
    override val stream: AsyncInput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charset: Charset = Charsets.UTF8,
    val closeParent: Boolean,
) : AbstractAsyncBufferedInput() {
    init {
        require(bufferSize > 0) { "bufferSize should be more than 0. bufferSize: $bufferSize" }
    }

    private val decoder = charset.newDecoder()
    private val charBuffer = CharBuffer.alloc(bufferSize)
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()

    suspend fun readANSIChar(): Char? {
        if (buffer.remaining == 0) {
            buffer.compact()
            fill()
        }
        if (buffer.remaining == 0) {
            return null
        }
        val c = buffer.getByte()
        return c.toInt().toChar()
    }

    suspend fun skipCRLF() {
        val r = readANSIChar()
        if (r != '\r') {
            throw IOException("Expected \"\\r\", but got \"$r\"")
        }
        val n = readANSIChar()
        if (n != '\n') {
            throw IOException("Expected \"\\n\", but got \"$n\"")
        }
    }

//    private fun dump(): String {
//        val sb = StringBuilder()
//        val p = buffer.position
//        val l = buffer.limit
//
//        fun c(c: Char): String =
//            when (c) {
//                '\r' -> "\\r"
//                '\n' -> "\\n"
//                '\t' -> "\\t"
//                0.toChar() -> "\\0"
//                else -> c.toString()
//            }
//
//        buffer.holdState { buffer ->
//            buffer.clear()
//            sb.append("$p-$l:")
//            repeat(buffer.capacity) { index ->
//                if (index == p && index == l) {
//                    sb.append(" [PL]")
//                } else {
//                    if (index == p) {
//                        sb.append(" [P]")
//                    }
//                    if (index == l) {
//                        sb.append(" [L]")
//                    }
//                }
//                sb.append(" ").append(c(buffer[index].toInt().toChar()))
//            }
//            if (l == buffer.capacity) {
//                if (p == l) {
//                    sb.append(" [PL]")
//                } else {
//                    sb.append(" [L]")
//                }
//            }
//            return sb.toString()
//        }
//    }

    private suspend fun loadMore(minSize: Int) {
        if (buffer.remaining >= minSize) {
            return
        }
        buffer.free()
        buffer.position = buffer.limit
        buffer.limit = buffer.capacity
        fill()
    }

    suspend fun readString(length: Int): String {
        var len = length
        val sb = StringBuilder()
        while (len > 0) {
            loadMore(len)
            charBuffer.clear()
            val before = buffer.position
            val p = buffer.limit
            buffer.limit = minOf(buffer.position + len, minOf(buffer.limit, buffer.capacity))
            decoder.decode(buffer, charBuffer)
            val read = buffer.position - before
            len -= read
            charBuffer.flip()
            sb.append(charBuffer.toString())
            buffer.limit = p
            buffer.free()
        }
        return sb.toString()
    }

    suspend fun readln(): String {
        val sb = StringBuilder()
        while (true) {
            if (buffer.remaining == 0) {
                loadMore(10)
            }
            charBuffer.clear()
            val endIndex = buffer.indexOfFirst { it == 10.toByte() }
            val p = buffer.limit
            buffer.limit = if (endIndex == -1) {
                buffer.limit
            } else {
                endIndex + 1
            }
            decoder.decode(buffer, charBuffer)
            charBuffer.flip()
            sb.append(charBuffer.toString())
            buffer.limit = p
            buffer.free()
            if (endIndex >= 0) {
                break
            }
        }
        return sb.toString().removeSuffix("\r\n")
    }

    override suspend fun asyncClose() {
        try {
            super.asyncClose()
            if (closeParent) {
                stream.asyncClose()
            }
        } finally {
            charBuffer.close()
            buffer.close()
        }
    }
}
