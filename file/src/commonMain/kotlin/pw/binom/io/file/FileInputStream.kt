package pw.binom.io.file

import pw.binom.io.InputStream

expect class FileInputStream(file:File): InputStream {
    override fun read(data: ByteArray, offset: Int, length: Int): Int
    override fun close()
}