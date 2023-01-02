package pw.binom.io

import pw.binom.security.MessageDigest

class AsyncCheckedInput(val stream: AsyncInput, val cksum: MessageDigest) : AsyncInput {
    override val available: Int
        get() = stream.available

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        var len = length
//        len = stream.read(data, offset, len)
//        if (len != -1) {
//            cksum.update(data, offset, len)
//        }
//        return len
//    }

    override suspend fun read(dest: ByteBuffer): Int {
        val pos = dest.position
        val ll = stream.read(dest)
        if (ll != -1) {
            dest.flip()
            cksum.update(dest)
        }
        return ll
    }

    override suspend fun asyncClose() {
        stream.asyncClose()
    }

//    override suspend fun skip(n: Long): Long {
//        val buf = ByteBuffer(512)
//        try {
//            var total: Long = 0
//            while (total < n) {
//                var len = n - total
//                buf.reset(0, if (len < buf.capacity) len.toInt() else buf.capacity)
//                len = read(buf).toLong()
//                if (len == -1L) {
//                    return total
//                }
//                total += len
//            }
//            return total
//        } finally {
//            buf.close()
//        }
//    }
}
