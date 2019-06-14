package pw.binom.process

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import pw.binom.io.OutputStream

class PipeOutput : Pipe(), OutputStream {

    override fun write(data: ByteArray, offset: Int, length: Int): Int =
            platform.posix.write(write, data.refTo(offset), length.convert()).convert()

    override fun flush() {
    }

    override fun close() {
    }
}