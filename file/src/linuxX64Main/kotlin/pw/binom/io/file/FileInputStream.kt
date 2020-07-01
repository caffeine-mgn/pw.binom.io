package pw.binom.io.file

import kotlinx.cinterop.convert
import kotlinx.cinterop.plus
import kotlinx.cinterop.refTo
import platform.posix.*
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.EOFException
import pw.binom.io.InputStream
import pw.binom.Input

actual class FileInputStream actual constructor(val file: File) : InputStream, Input {

    private val static = ByteArray(1)

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

    override fun read(dest: ByteBuffer): Int {
        if (feof(handler) != 0)
            return 0
        val r = fread(dest.native+dest.position, 1.convert(), dest.remaining.convert(), handler).convert<Int>()
        dest.position+=r
        return r
    }

    init {
        if (!file.isFile)
            throw FileNotFoundException(file.path)
    }

    internal val handler = fopen(file.path, "rb")
    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (feof(handler) != 0)
            return 0
        return fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert()
    }

    override fun read(): Byte {
        if (read(static) != 1)
            throw EOFException()
        return static[0]
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fread(data.refTo(offset), 1.convert(), length.convert(), handler).convert()
//    }

    override fun close() {
        fclose(handler)
    }
}