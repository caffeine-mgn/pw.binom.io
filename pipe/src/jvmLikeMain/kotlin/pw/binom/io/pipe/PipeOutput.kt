package pw.binom.io.pipe

import pw.binom.io.ByteBuffer
import pw.binom.io.Output
import java.io.PipedOutputStream
import java.nio.channels.Channels

actual class PipeOutput private constructor(val native: PipedOutputStream) : Output {

    val channel = Channels.newChannel(native)

    actual constructor(input: PipeInput) : this(PipedOutputStream(input.native))

    actual constructor() : this(PipedOutputStream())

    override fun write(data: ByteBuffer): Int =
        channel.write(data.native)

    override fun flush() {
        native.flush()
    }

    override fun close() {
        native.close()
    }
}
