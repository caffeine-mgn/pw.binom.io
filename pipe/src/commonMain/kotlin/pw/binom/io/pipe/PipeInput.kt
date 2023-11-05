package pw.binom.io.pipe

import pw.binom.io.Input

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PipeInput : Input {
  constructor()
  constructor(output: PipeOutput)
}
