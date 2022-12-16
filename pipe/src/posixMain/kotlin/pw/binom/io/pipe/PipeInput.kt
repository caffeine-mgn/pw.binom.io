package pw.binom.io.pipe

import kotlinx.cinterop.convert
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Input

actual class PipeInput private constructor(fd: IntArray) : Input {
    init {
        require(fd.size == 2)
    }

    internal var writeFd: Int = fd[0]
    internal var readFd: Int = fd[1]

    private var endded = AtomicBoolean(false)

    actual constructor() : this(createPipe())

    actual constructor(output: PipeOutput) : this(intArrayOf(output.writeFd, output.readFd))

    override fun read(dest: ByteBuffer): Int {
        if (endded.getValue()) {
            return 0
        }

        val r = dest.ref { destPtr, remaining ->
            if (remaining > 0) {
                platform.posix.read(readFd, destPtr, remaining.convert()).convert<Int>()
            } else {
                0
            }
        } ?: 0
        if (r <= 0) {
            endded.setValue(true)
        } else {
            dest.position += r
        }
        return r
    }

    override fun close() {
        platform.posix.close(readFd)
    }
}
