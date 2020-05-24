package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicInt

open class BufferedInputStream(val stream: InputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE) : InputStream {
    private val buffer = ByteArray(bufferSize)
    private var readed = AtomicInt(0)
    private var wrote = AtomicInt(0)

    override val available: Int
        get() {
            if (wrote.value == 0 || wrote.value == readed.value)
                return -1
            return wrote.value - readed.value
        }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (available == -1) {
            readed.value = 0
            wrote.value = stream.read(buffer, 0, buffer.size)
            if (wrote.value <= 0)
                return wrote.value
        }

        val l = minOf(wrote.value - readed.value, length)

        buffer.copyInto(data, destinationOffset = offset, startIndex = readed.value, endIndex = readed.value + l)
        readed.addAndGet(l)
        return l
    }

    override fun close() {
        stream.close()
    }
}

fun InputStream.buffered(bufferSize: Int = DEFAULT_BUFFER_SIZE) = BufferedInputStream(this, bufferSize)