package pw.binom.io.pipe

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Output

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PipeOutput : Output {
  constructor()
  constructor(input: PipeInput)

  override fun close()
  override fun flush()
  override fun write(data: ByteBuffer): DataTransferSize
}
