package pw.binom.process

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import pw.binom.io.InputStream

class PipeInput : Pipe(), InputStream {

    private var endded = false
    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (endded)
            return 0
//        println("Try to read $length")
        val r = platform.posix.read(read, data.refTo(offset), length.convert()).convert<Int>()
        if (r <= 0)
            endded = true
        return r
    }

    override val available: Int
        get() = if (endded) 0 else -1

    override fun close() {
    }
}