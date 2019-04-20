package pw.binom.io.file

import pw.binom.io.OutputStream

expect class FileOutputStream(file:File,append:Boolean=true): OutputStream {
    override fun write(data: ByteArray, offset: Int, length: Int): Int
    override fun close()
}