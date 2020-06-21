package pw.binom.io.file

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import pw.binom.io.OutputStream
import kotlin.native.concurrent.freeze

actual class FileOutputStream actual constructor(file: File, append: Boolean) : OutputStream {
    override fun flush() {
        //NOP
    }

    private val handler = fopen(file.path, if (append) "a+" else "w+") ?: throw FileNotFoundException(file.path)

    actual override fun write(data: ByteArray, offset: Int, length: Int): Int =
            fwrite(data.refTo(offset), 1.convert(), length.convert(), handler).convert()

    actual override fun close() {
        fclose(handler)
    }

    init {
        freeze()
    }
}