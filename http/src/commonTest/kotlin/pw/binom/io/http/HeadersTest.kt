package pw.binom.io.http

import pw.binom.testing.shouldEquals
import pw.binom.testing.shouldNull
import kotlin.test.Test

class HeadersTest {
  @Test
  fun parseCharsetTest() {
    listOf(
      "text/plain;charset=utf-8",
      "text/plain;charset=UTF-8",
      "text/plain;charset=UtF-8",
      "text/plain;version=0.0.4;charset=utf-8"
    ).forEach {
      headersOf(
        Headers.CONTENT_TYPE to it
      ).charset shouldEquals "UTF-8"
    }
    listOf(
      "",
      "text/plain",
      "text/plain;version=0.0.4",
      "text/plain;version=0.0.4;my-charset=utf-8",
      "text/plain;my-charset=utf-8"
    ).forEach {
      headersOf(
        Headers.CONTENT_TYPE to it
      ).charset.shouldNull()
    }
  }
}
