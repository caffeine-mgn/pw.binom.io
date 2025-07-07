package pw.binom

import pw.binom.io.Input
import pw.binom.io.Output
import pw.binom.io.Reader
import pw.binom.io.Writer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object Console {
  val stdChannel: Output
  val errChannel: Output
  val inChannel: Input

  val std: Writer
  val err: Writer
  val input: Reader
}
