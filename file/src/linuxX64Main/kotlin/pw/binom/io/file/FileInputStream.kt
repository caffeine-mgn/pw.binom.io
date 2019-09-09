package pw.binom.io.file

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.*
import pw.binom.io.InputStream

actual class FileInputStream actual constructor(file: File) : InputStream {
    override fun skip(length: Long): Long {
        if (length == 0L)
            return 0L
        if (feof(handler) != 0)
            return 0L
        val currentPos = ftell(handler).convert<Long>()
        fseek(handler, 0, SEEK_END)
        val endOfFile = ftell(handler).convert<Long>()
        val position = minOf(endOfFile, currentPos + length)
        fseek(handler, position.convert(), SEEK_SET)
        return endOfFile - position
    }

    init {
        if (!file.isFile)
            throw FileNotFoundException(file.path)
    }

    internal val handler = fopen(file.path, "rb")
    actual override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (feof(handler) != 0)
            return -1
        return fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert()
    }

    actual override fun close() {
        fclose(handler)
    }
}