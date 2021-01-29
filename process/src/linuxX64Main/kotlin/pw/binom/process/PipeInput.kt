package pw.binom.process

import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import pw.binom.ByteBuffer
import pw.binom.Input
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze

class PipeInput : Pipe(), Input {

    private var endded = AtomicBoolean(false)

    override fun read(dest: ByteBuffer): Int {
        if (endded.value)
            return 0
        val l = dest.remaining
        if (l == 0)
            return 0
        val r = platform.posix.read(read, dest.refTo(dest.position), l.convert()).convert<Int>()
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