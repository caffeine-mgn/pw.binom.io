package pw.binom.io.httpClient

sealed interface ResponseLength {
  object None : ResponseLength {
    override fun toString(): String = "ResponseLength.None"
  }

  object Chunked : ResponseLength {
    override fun toString(): String = "ResponseLength.Chunked"
  }

  class Fixed(val length: Long) : ResponseLength {
    override fun toString(): String = "ResponseLength.Fixed($length)"
  }
}
