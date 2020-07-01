package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.Input

class CheckedInput(val stream: Input, val cksum: CRC32Basic) : Input {

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        var len = length
//        len = stream.read(data, offset, len)
//        if (len != -1) {
//            cksum.update(data, offset, len)
//        }
//        return len
//    }

    override fun close() {
        stream.close()
    }

    override fun skip(n: Long): Long {
        val buf = ByteBuffer.alloc(512)
        try {
            var total: Long = 0
            while (total < n) {
                var len = n - total
                buf.reset(0, if (len < buf.capacity) len.toInt() else buf.capacity)
                len = read(buf).toLong()
                if (len == -1L) {
                    return total
                }
                total += len
            }
            return total
        } finally {
            buf.close()
        }
    }

    override fun read(dest: ByteBuffer): Int {
        val pos = dest.position
        val len = dest.remaining
        val ll = stream.read(dest)
        if (ll != -1) {
            cksum.update(dest, pos, len)
        }
        return len
    }
}