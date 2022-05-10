package pw.binom.io

interface Input : Closeable {
    fun read(dest: ByteBuffer): Int
    fun readFully(dest: ByteBuffer): Int {
        val length = dest.remaining123
        while (dest.remaining123 > 0 && dest.remaining123 > 0) {
            if (read(dest) == 0) {
                throw EOFException()
            }
        }
        return length
    }
}
