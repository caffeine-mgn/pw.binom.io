package pw.binom.process

import kotlinx.cinterop.convert
import pw.binom.ByteBuffer
import pw.binom.Input
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze

class PipeInput : Pipe(), Input {

    private var endded = AtomicBoolean(false)

    override fun read(dest: ByteBuffer): Int {
        if (endded.value)
            return 0

        val r = dest.ref { destPtr, remaining ->
            if (remaining>0) {
                platform.posix.read(read, destPtr, remaining.convert()).convert<Int>()
            } else{
                0
            }
        }
        if (r <= 0)
            endded.value = true
        else {
            dest.position += r
        }
        return r
    }

    override fun close() {
    }

    init{
        doFreeze()
    }
}