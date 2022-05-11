package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class BufferedInput(val stream: Input, bufferSize: Int = DEFAULT_BUFFER_SIZE) : Input {
    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    val available
        get() = if (buffer.remaining == 0) -1 else buffer.remaining

    override fun read(dest: ByteBuffer): Int {
        if (buffer.remaining == 0) {
            buffer.clear()
            stream.read(buffer)
            buffer.flip()
        }
        return dest.write(buffer)
    }

    override fun close() {
        buffer.close()
        stream.close()
    }
}

fun Input.bufferedInput(bufferSize: Int = DEFAULT_BUFFER_SIZE) = BufferedInput(this, bufferSize)
