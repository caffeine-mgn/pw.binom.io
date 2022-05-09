package pw.binom.io.file

import pw.binom.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.StreamClosedException
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

actual class FileChannel actual constructor(file: File, vararg mode: AccessType) :
    Channel,
    RandomAccess {

    private val native = FileChannel.open(
        file.native.toPath(),
        mode.asSequence().map {
            when (it) {
                AccessType.APPEND -> StandardOpenOption.APPEND
                AccessType.CREATE -> StandardOpenOption.CREATE
                AccessType.READ -> StandardOpenOption.READ
                AccessType.WRITE -> StandardOpenOption.WRITE
            }
        }.toSet()
    )

    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    actual fun skip(length: Long): Long {
        checkClosed()
        val l = minOf(native.position() + length, native.size())
        native.position(l)
        return l
    }

    override fun read(dest: ByteBuffer): Int {
        checkClosed()
        return native.read(dest.native).let {
            if (it == -1) 0 else it
        }
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset, length) { data ->
//            native.read(data)
//        }
//    }

    override fun close() {
        checkClosed()
        closed = true
        native.close()
    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        return native.write(data.native)
    }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset, length) { data ->
//            native.write(data)
//        }
//    }

    override fun flush() {
        checkClosed()
        native.force(true)
    }

    override var position: Long
        get() = native.position()
        set(value) {
            checkClosed()
            native.position(value)
        }

    override val size: Long
        get() {
            checkClosed()
            return native.size()
        }
}
