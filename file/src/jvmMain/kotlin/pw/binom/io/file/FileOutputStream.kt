package pw.binom.io.file

import pw.binom.io.IOException
import pw.binom.io.OutputStream
import java.io.FileOutputStream as JFileOutputStream
import java.io.IOException as JIOException

actual class FileOutputStream actual constructor(file: File, append: Boolean) : OutputStream {
    override fun flush() {
        native.flush()
    }

    internal val native = JFileOutputStream(file.native, append)
    actual override fun write(data: ByteArray, offset: Int, length: Int): Int =
            try {
                native.write(data, offset, length)
                length
            } catch (e: JIOException) {
                throw IOException(cause = e)
            }

    actual override fun close() {
        try {
            native.close()
        } catch (e: JIOException) {
            throw IOException(cause = e)
        }
    }
}