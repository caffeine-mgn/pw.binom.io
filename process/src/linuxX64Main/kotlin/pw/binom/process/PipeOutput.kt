package pw.binom.process

import kotlinx.cinterop.convert
import kotlinx.cinterop.plus
import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.doFreeze

class PipeOutput : Pipe(), Output {

    override fun write(data: ByteBuffer): Int {
        val l = data.remaining
        if (l == 0)
            return 0
        val wrote = platform.posix.write(write, data.native + data.position, l.convert()).convert<Int>()
        data.position += wrote
        return wrote
    }

    override fun flush() {
    }

    override fun close() {
    }

    init {
        doFreeze()
    }
}