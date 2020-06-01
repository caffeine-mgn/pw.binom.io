package pw.binom.io.file

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.*
import pw.binom.io.IOException
import pw.binom.io.EOFException
import pw.binom.io.InputStream

actual class FileInputStream actual constructor(file: File) : InputStream {
    private val static = ByteArray(1)

    init {
        if (!file.isFile)
            throw FileNotFoundException(file.path)
    }

    override fun skip(length: Long): Long {
        if (length == 0L)
            return 0L
        if (feof(handler) != 0)
            return 0L
        val currentPos = ftello64(handler)
        fseek(handler, 0, SEEK_END)
        val endOfFile = ftello64(handler)
        val position = minOf(endOfFile, currentPos + length)
        fseeko64(handler, position, SEEK_SET)
        return endOfFile - position
    }

    override fun read(): Byte {
        if (read(static) != 1)
            throw EOFException()
        return static[0]
    }

    internal val handler = fopen(file.path, "rb") ?: throw FileNotFoundException(file.path)

    actual override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (feof(handler) != 0)
            return -1
        val read = fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert<Int>()
        if (read < length && feof(handler) <= 0) {
            throw IOException("Can't read file. Error: ${ferror(handler)}")
        }
        return read
    }

    actual override fun close() {
        fclose(handler)
    }
}