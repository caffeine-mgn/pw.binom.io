package pw.binom.io.pipe

import pw.binom.io.ByteBuffer
import pw.binom.io.Input
import java.io.PipedInputStream
import java.nio.channels.Channels

actual class PipeInput private constructor(val native: PipedInputStream) : Input {

    val channel = Channels.newChannel(native)

    actual constructor(output: PipeOutput) : this(PipedInputStream(output.native))

    actual constructor() : this(PipedInputStream())

    override fun read(dest: ByteBuffer): Int = channel.read(dest.native)

    override fun close() {
        native.close()
    }
}
