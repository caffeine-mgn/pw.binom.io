package pw.binom.io.http

object TestData {
  val wikipediaData = charArrayOf(
    'W', 'i', 'k', 'i', 'p', 'e', 'd', 'i', 'a',
  )

  /**
   * Chunked message as
   * `Wiki` and `pedia`
   */
  val wikipediaChunkedData = charArrayOf(
    '4', '\r', '\n',
    'W', 'i', 'k', 'i',
    '\r',
    '\n',
    '5', '\r', '\n',
    'p', 'e', 'd', 'i', 'a',
    '\r',
    '\n',
    '0', '\r', '\n',
    '\r',
    '\n',
  )
}
