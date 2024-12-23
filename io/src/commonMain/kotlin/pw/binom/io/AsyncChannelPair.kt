package pw.binom.io

interface AsyncChannelPair<IN : AsyncInput, OUT : AsyncOutput> : AsyncCloseable {
  val input: IN
  val output: OUT

  companion object {
    fun <IN : AsyncInput, OUT : AsyncOutput> create(
      input: IN,
      output: OUT,
    ) =
      object : AsyncChannelPair<IN, OUT> {
        override val input: IN
          get() = input
        override val output: OUT
          get() = output
      }
  }

  override suspend fun asyncClose() {
    try {
      input.asyncClose()
    } finally {
      output.asyncClose()
    }
  }
}


