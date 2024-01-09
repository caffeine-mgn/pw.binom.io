package pw.binom.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class LinkedListTest {
  @Test
  fun subListTest() {
    val list = linkedListOf(0, 1, 2, 3, 4)
    val result = list.subList(fromIndex = 1, toIndex = 3)
    assertEquals(3, result.size)
    assertEquals(1, result[0])
    assertEquals(2, result[1])
    assertEquals(3, result[2])
    list.forEachLinked { }
  }
}
