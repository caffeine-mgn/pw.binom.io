package pw.binom.io.file

import pw.binom.ByteBuffer
import pw.binom.io.Channel
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

actual class FileChannel actual constructor(file: File, vararg mode: AccessType) : Channel, FileAccess {

    private val native = FileChannel.open(file.native.toPath(), mode.asSequence().map {
        when (it) {
            AccessType.APPEND -> StandardOpenOption.APPEND
            AccessType.CREATE -> StandardOpenOption.CREATE
            AccessType.READ -> StandardOpenOption.READ
            AccessType.WRITE -> StandardOpenOption.WRITE
        }
    }.toSet())

    override fun skip(length: Long): Long {
        val l = minOf(native.position() + length, native.size())
        native.position(l)
        return l
    }

    override fun read(dest: ByteBuffer): Int =
            native.read(dest.native).let {
                if (it == -1) 0 else it
            }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset, length) { data ->
//            native.read(data)
//        }
//    }

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int =
            native.write(data.native)

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset, length) { data ->
//            native.write(data)
//        }
//    }

    override fun flush() {
    }

    override var position: ULong
        get() = native.position().toULong()
        set(value) {
            native.position(value.toLong())
        }

    override val size: ULong
        get() = native.size().toULong()

}