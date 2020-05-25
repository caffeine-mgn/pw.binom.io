package pw.binom.process

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.InputStream

class PipeInput : Pipe(), InputStream {

    private var endded = AtomicBoolean(false)
    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (endded.value)
            return 0
        val r = platform.posix.read(read, data.refTo(offset), length.convert()).convert<Int>()
        if (r <= 0)
            endded.value = true
        return r
    }

    override val available: Int
        get() = if (endded.value) 0 else -1

    override fun close() {
    }
}