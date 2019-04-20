package pw.binom.io.file

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.fclose
import platform.posix.feof
import platform.posix.fopen
import platform.posix.fread
import pw.binom.io.InputStream

actual class FileInputStream actual constructor(file: File) : InputStream {
    init {
        if (!file.isFile)
            throw FileNotFoundException(file.path)
    }

    internal val handler = fopen(file.path, "rb")
    actual override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (feof(handler) !=0)
            return -1
        return fread(data.refTo(offset.convert()), 1, length.convert(), handler).convert()
    }

    actual override fun close() {
        fclose(handler)
    }
}