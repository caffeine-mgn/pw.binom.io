package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.Input
import pw.binom.empty

class BufferedInput(val stream: Input, bufferSize: Int = DEFAULT_BUFFER_SIZE) : Input {
    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    val available
        get() = if (buffer.remaining == 0) -1 else buffer.remaining

//    override fun skip(length: Long): Long {
//        val buf = ByteBuffer.alloc(512)
//        try {
//            var l = length
//            while (l > 0) {
//                buf.reset(0, minOf(buf.capacity, l.toInt()))
//                l -= read(buf)
//            }
//        } finally {
//            buf.close()
//        }
//        return length
//    }

    override fun read(dest: ByteBuffer): Int {
        if (buffer.remaining == 0) {
            buffer.clear()
            stream.read(buffer)
            buffer.flip()
        }
        return dest.write(buffer)
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (available == -1) {
//            readed = 0
//            wrote = stream.read(buffer, 0, buffer.size)
//            if (wrote <= 0)
//                return wrote
//        }
//
//        val l = minOf(wrote - readed, length)
//
//        buffer.writeTo(readed, data, offset, l)
////        buffer.copyInto(data, destinationOffset = offset, startIndex = readed.value, endIndex = readed.value + l)
//        readed += l
//        return l
//    }

    override fun close() {
        buffer.close()
        stream.close()
    }
}

fun Input.bufferedInput(bufferSize: Int = DEFAULT_BUFFER_SIZE) = BufferedInput(this, bufferSize)