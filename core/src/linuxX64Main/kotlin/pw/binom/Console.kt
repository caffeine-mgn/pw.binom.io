package pw.binom

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.io.AppendableUTF82
import pw.binom.io.Reader
import pw.binom.io.ReaderUTF82

//private val tmp1 = ByteBuffer.alloc(32)

actual object Console {

    private class Out(val fd: Int) : Output {

        override fun close() {
        }

//        override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//                platform.posix.write(fd, data.refTo(offset), length.convert()).convert()

        override fun write(data: ByteBuffer): Int =
                platform.posix.write(fd, data.native + data.position, data.remaining.convert()).convert()

        override fun flush() {
        }
    }

    actual val stdChannel: Output = Out(STDOUT_FILENO)
    actual val errChannel: Output = Out(STDERR_FILENO)

    actual val inChannel: Input = object : Input {
//        override fun skip(length: Long): Long {
//            var l = length
//            while (l > 0) {
//                tmp1.reset(0,minOf(tmp1.capacity, l.toInt()))
//                l -= read(tmp1)
//            }
//            return length
//        }

        override fun read(dest: ByteBuffer): Int =
                platform.posix.write(STDIN_FILENO, dest.native + dest.position, dest.remaining.convert()).convert()

//        override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int =
//                platform.posix.write(STDIN_FILENO, data.refTo(offset), length.convert()).convert()

        override fun close() {
        }

    }
    actual val std: Appendable = AppendableUTF82(stdChannel)
    actual val err: Appendable = object : Appendable {
        override fun append(value: Char): Appendable {
            memScoped {
                val v = value.toString().wcstr
                fwprintf(stdout, v.ptr.reinterpret(), v.size)
            }
            return this
        }

        override fun append(value: CharSequence?): Appendable {
            value ?: return this
            memScoped {
                val v = value.toString().wcstr
                fwprintf(stdout, v.ptr.reinterpret(), v.size)
            }
            return this
        }

        override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
            value ?: return this
            memScoped {
                val v = value.substring(startIndex, endIndex).wcstr
                fwprintf(stdout, v.ptr.reinterpret(), v.size)
            }
            return this
        }
    }
    actual val input: Reader = ReaderUTF82(inChannel)
}