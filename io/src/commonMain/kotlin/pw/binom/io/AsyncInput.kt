package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import kotlin.coroutines.cancellation.CancellationException

interface AsyncInput : AsyncCloseable {
    /**
     * Available Data size in bytes
     * @return Available data in bytes. If returns value less 0 it's mean that size of available data is unknown
     */
    val available: Int

    suspend fun skipAll(bufferSize: Int = DEFAULT_BUFFER_SIZE) {
        ByteBuffer(bufferSize).use { buffer ->
            skipAll(buffer = buffer)
        }
    }

    suspend fun skipAll(buffer: ByteBuffer) {
        while (true) {
            buffer.clear()
            if (read(buffer) == 0) {
                break
            }
        }
    }

    @Throws(EOFException::class, CancellationException::class)
    suspend fun skip(bytes: Long, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
        ByteBuffer(bufferSize).use { buffer ->
            skip(bytes = bytes, buffer = buffer)
        }
    }

    @Throws(EOFException::class, CancellationException::class)
    suspend fun skip(bytes: Long, buffer: ByteBuffer) {
        var skipRemaining = bytes
        while (skipRemaining > 0) {
            val forRead = minOf(buffer.capacity, skipRemaining.toInt())
            buffer.position = 0
            buffer.limit = forRead
            readFully(buffer)
            skipRemaining -= forRead
        }
    }

    suspend fun read(dest: ByteBuffer): Int
    suspend fun readFully(dest: ByteBuffer): Int {
        val length = dest.remaining
        while (dest.remaining > 0) {
            val read = read(dest)
            if (read == 0 && dest.remaining > 0) {
                throw EOFException("Full message $length bytes, can't read ${dest.remaining} bytes")
            }
        }
        return length
    }
}
