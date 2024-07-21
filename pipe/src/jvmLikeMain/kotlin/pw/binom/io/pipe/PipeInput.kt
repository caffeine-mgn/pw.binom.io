package pw.binom.io.pipe

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import java.io.PipedInputStream
import java.nio.channels.Channels

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PipeInput private constructor(val native: PipedInputStream) : Input {

  val channel = Channels.newChannel(native)

  actual constructor(output: PipeOutput) : this(PipedInputStream(output.native))

  actual constructor() : this(PipedInputStream())

  override fun read(dest: ByteBuffer): DataTransferSize {
    val readed = channel.read(dest.native)
    return when {
      readed == 0 -> DataTransferSize.EMPTY
      readed < 0 -> DataTransferSize.CLOSED
      else -> DataTransferSize.ofSize(readed)
    }
  }

  override fun close() {
    native.close()
  }
}
