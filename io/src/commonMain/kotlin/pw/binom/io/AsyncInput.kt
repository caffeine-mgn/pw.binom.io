package pw.binom.io

interface AsyncInput : AsyncCloseable {
    /**
     * Available Data size in bytes
     * @return Available data in bytes. If returns value less 0 it's mean that size of available data is unknown
     */
    val available: Int

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
