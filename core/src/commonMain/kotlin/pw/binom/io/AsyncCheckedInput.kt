package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput

class AsyncCheckedInput(val stream: AsyncInput, val cksum: CRC32Basic) : AsyncInput {

    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        var len = length
        len = stream.read(data, offset, len)
        if (len != -1) {
            cksum.update(data, offset, len)
        }
        return len
    }

    override suspend fun close() {
        stream.close()
    }

    override suspend fun skip(n: Long): Long {
        val buf = ByteDataBuffer.alloc(512)
        var total: Long = 0
        while (total < n) {
            var len = n - total
            len = read(buf, 0, if (len < buf.size) len.toInt() else buf.size).toLong()
            if (len == -1L) {
                return total
            }
            total += len
        }
        return total
    }
}