package pw.binom.io.pipe

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.IOException
import pw.binom.io.Output
import java.io.PipedOutputStream
import java.nio.channels.Channels

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PipeOutput private constructor(val native: PipedOutputStream) : Output {

  val channel = Channels.newChannel(native)

  actual constructor(input: PipeInput) : this(PipedOutputStream(input.native))

  actual constructor() : this(PipedOutputStream())

  actual override fun write(data: ByteBuffer): DataTransferSize =
    try {
      DataTransferSize.ofSize(channel.write(data.native))
    } catch (e: IOException) {
      DataTransferSize.CLOSED
    }

  actual override fun flush() {
    native.flush()
  }

  actual override fun close() {
    native.close()
  }
}
