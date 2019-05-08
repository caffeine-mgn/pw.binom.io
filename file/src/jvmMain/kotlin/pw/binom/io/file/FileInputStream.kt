package pw.binom.io.file

import pw.binom.io.IOException
import pw.binom.io.InputStream
import java.io.EOFException as JEOFException
import java.io.FileInputStream as JFileInputStream
import java.io.IOException as JIOException

actual class FileInputStream actual constructor(file: File) : InputStream {

    init {
        if (!file.native.isFile)
            throw FileNotFoundException(file.path)
    }

    internal val native = JFileInputStream(file.native)

    actual override fun read(data: ByteArray, offset: Int, length: Int): Int =
            try {
                native.read(data, offset, length)
            } catch (e: JEOFException) {
                throw IOException(cause = e)
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