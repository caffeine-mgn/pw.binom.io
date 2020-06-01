package pw.binom.io

class CheckedInputStream(val stream: InputStream, val cksum: CRC32Basic) : InputStream {

    override fun read(): Byte {
        val r = stream.read()
        cksum.update(r)
        return r
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        var len = length
        len = stream.read(data, offset, len)
        if (len != -1) {
            cksum.update(data, offset, len)
        }
        return len
    }

    override fun close() {
        stream.close()
    }

    override fun skip(n: Long): Long {
        val buf = ByteArray(512)
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