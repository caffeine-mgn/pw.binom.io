package pw.binom.io.file

import kotlinx.cinterop.convert
import platform.posix.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlinx.cinterop.*
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Channel

actual class FileChannel actual constructor(file: File, vararg mode: AccessType) : Channel, FileAccess {

    init {
        if (AccessType.CREATE !in mode && !file.isFile)
            throw FileNotFoundException(file.path)
    }

    internal val handler = fopen(file.path, run {
        val read = AccessType.READ in mode
        val write = AccessType.WRITE in mode
        val append = AccessType.APPEND in mode
        if (!read && !write)
            throw IllegalArgumentException("Invalid mode")
        when {
            write && !append -> {
                if (read) "wb+" else "wb"
            }
            write && append -> {
                if (read) "cb+" else "cb"
            }
            read -> "rb"
            else -> throw IllegalArgumentException("Invalid mode")
        }
    }) ?: throw FileNotFoundException(file.path)

    actual fun skip(length: Long): Long {
        if (length == 0L)
            return 0L
        if (feof(handler) != 0)
            return 0L
        val endOfFile = size
        val position = minOf(endOfFile, this.position + length.toULong())
        this.position = position
        return (endOfFile - position).toLong()
    }

    override fun read(dest: ByteBuffer): Int {
        if (feof(handler) != 0)
            return 0
        val l = fread((dest.native + dest.position), 1.convert(), dest.remaining.convert(), handler).convert<Int>()
        dest.position += l
        return l
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert()
//    }

    override fun close() {
        fclose(handler)
    }

    override fun write(data: ByteBuffer): Int {
        if (feof(handler) != 0)
            return 0
        return fwrite(data.native+data.position, 1.convert(), data.remaining.convert(), handler).convert()
    }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fwrite(data.refTo(offset), 1.convert(), length.convert(), handler).convert()
//    }

    override fun flush() {
    }

    private fun gotoEnd() {
        fseek(handler, 0, SEEK_END)
    }

    override var position: ULong
        get() = ftell(handler).convert()
        set(value) {
            fseek(handler, value.convert(), SEEK_SET)
        }
    override val size: ULong
        get() {
            val pos = position
            gotoEnd()
            val result = position
            position = pos
            return result
        }
}