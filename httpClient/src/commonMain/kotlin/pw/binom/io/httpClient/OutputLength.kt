package pw.binom.io.httpClient

sealed interface OutputLength {
    object None : OutputLength
    object Chunked : OutputLength
    class Fixed(val length: Long) : OutputLength
}
