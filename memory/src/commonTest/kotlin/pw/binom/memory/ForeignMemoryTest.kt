package pw.binom.memory

import kotlin.test.Test

class ForeignMemoryTest {
  @Test
  fun test() {
    ForeignMemory.allocate(14uL).use { mem ->
      mem.setLong(0uL, 10L)
      println("-->${mem.getLong(0uL)}")
    }
  }
}
