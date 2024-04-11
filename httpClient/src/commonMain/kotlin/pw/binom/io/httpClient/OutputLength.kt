package pw.binom.io.httpClient

sealed interface OutputLength {
  object None : OutputLength {
    override fun toString(): String = "OutputLength.None"
  }

  object Chunked : OutputLength {
    override fun toString(): String = "OutputLength.Chunked"
  }

  class Fixed(val length: Long) : OutputLength {
    override fun toString(): String = "OutputLength.Fixed($length)"
  }
}
