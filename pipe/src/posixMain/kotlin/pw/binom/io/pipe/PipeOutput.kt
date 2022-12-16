package pw.binom.io.pipe

import kotlinx.cinterop.convert
import pw.binom.io.ByteBuffer
import pw.binom.io.Output

actual class PipeOutput private constructor(fd: IntArray) : Output {
    init {
        require(fd.size == 2)
    }

    internal var writeFd: Int = fd[0]
    internal var readFd: Int = fd[1]

    actual constructor() : this(createPipe())

    actual constructor(input: PipeInput) : this(intArrayOf(input.writeFd, input.readFd))

    override fun write(data: ByteBuffer): Int {
        val wrote = data.ref { dataPtr, remaining ->
            if (remaining > 0) {
                platform.posix.write(writeFd, dataPtr, remaining.convert()).convert<Int>()
            } else {
                0
            }
        } ?: 0
        data.position += wrote
        return wrote
    }

    override fun flush() {
        // Do nonthing
    }

    override fun close() {
        platform.posix.close(writeFd)
    }
}
