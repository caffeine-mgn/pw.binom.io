package pw.binom.process

import kotlinx.cinterop.convert
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Input

class PipeInput : Pipe(), Input {

    private var endded = AtomicBoolean(false)

    override fun read(dest: ByteBuffer): Int {
        if (endded.getValue()) {
            return 0
        }

        val r = dest.ref(0) { destPtr, remaining ->
            if (remaining > 0) {
                platform.posix.read(read, destPtr, remaining.convert()).convert<Int>()
            } else {
                0
            }
        }
        if (r <= 0) {
            endded.setValue(true)
        } else {
            dest.position += r
        }
        return r
    }

    override fun close() {
    }
}
