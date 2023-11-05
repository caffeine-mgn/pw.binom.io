package pw.binom.io.pipe

import pw.binom.io.Output

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PipeOutput : Output {
  constructor()
  constructor(input: PipeInput)
}
