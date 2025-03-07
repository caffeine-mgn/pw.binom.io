package pw.binom.io.http

sealed interface HttpContentLength {
  data object CHUNKED : HttpContentLength
  data object NONE : HttpContentLength
  data class Fixed(val size: ULong, val chunked: Boolean) : HttpContentLength
}

private fun calcHttpContentLength(
  contentLength: ULong?,
  chunked: Boolean,
) = when {
  contentLength != null -> HttpContentLength.Fixed(size = contentLength, chunked = chunked)
  chunked -> HttpContentLength.CHUNKED
  else -> HttpContentLength.NONE
}

val SimpleHeaders.httpContentLength: HttpContentLength
  get() = calcHttpContentLength(
    contentLength = getFirstOrNull(Headers.CONTENT_LENGTH)?.toULongOrNull(),
    chunked = any { key, value ->
      key == Headers.TRANSFER_ENCODING && value == Encoding.CHUNKED
    },
  )

val Headers.httpContentLength: HttpContentLength
  get() = calcHttpContentLength(
    contentLength = contentLength,
    chunked = transferEncoding == Encoding.CHUNKED
  )

var MutableHeaders.httpContentLength: HttpContentLength
  get() = (this as Headers).httpContentLength
  set(value) {
    when (value) {
      HttpContentLength.NONE -> {
        contentLength = null
        transferEncoding = null
      }

      HttpContentLength.CHUNKED -> {
        contentLength = null
        transferEncoding = Encoding.CHUNKED
      }

      is HttpContentLength.Fixed -> {
        contentLength = value.size
        transferEncoding = if (value.chunked) Encoding.CHUNKED else null
      }
    }
  }
