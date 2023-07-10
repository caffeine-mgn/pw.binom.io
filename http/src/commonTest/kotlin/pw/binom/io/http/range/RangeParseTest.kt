package pw.binom.io.http.range

import kotlin.test.Test

class RangeParseTest {
  @Test
  fun test() {
    val ranges = Range.parseRange("bytes=0-")
    println("ranges: $ranges")
//        assertEquals(1, ranges.size)
  }
}
