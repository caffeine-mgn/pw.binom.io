package pw.binom.io.file

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.*
import pw.binom.io.InputStream

actual class FileInputStream actual constructor(file: File) : InputStream {
    init {
        if (!file.isFile)
            throw FileNotFoundException(file.path)
    }

    override fun skip(length: Long):Long {
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

    internal val handler = fopen(file.path, "rb") ?: throw FileNotFoundException(file.path)

    actual override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (feof(handler) != 0)
            return -1
        return fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert()
    }

    actual override fun close() {
        fclose(handler)
    }
}