package pw.binom.process

import kotlinx.cinterop.convert
import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.doFreeze

class PipeOutput : Pipe(), Output {

    override fun write(data: ByteBuffer): Int {
        val wrote = data.ref { dataPtr, remaining ->
            if (remaining > 0) {
                platform.posix.write(write, dataPtr, remaining.convert()).convert<Int>()
            } else {
                0
            }
        } ?: 0
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
