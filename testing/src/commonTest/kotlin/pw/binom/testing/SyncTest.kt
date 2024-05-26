package pw.binom.testing

import kotlin.test.Test

class SyncTest {
  @Test
  fun runOneEnabled() {
    var counter = 0
    var counter1 = 0
    var counter2 = 0
    var counter3 = 0
    var counter4 = 0
    Testing.sync {
      counter++
      test("1", ignore = true) { counter1++ }
      test("2", ignore = true) { counter2++ }
      test("3", ignore = false) { counter3++ }
      test("4", ignore = true) { counter4++ }
    }
    counter1 shouldEquals 0
    counter2 shouldEquals 0
    counter3 shouldEquals 1
    counter4 shouldEquals 0
    counter shouldEquals 1
  }

  @Test
  fun noEnabled() {
    var counter = 0
    var counter1 = 0
    var counter2 = 0
    var counter3 = 0
    var counter4 = 0
    Testing.sync {
      counter++
      test("1", ignore = true) { counter1++ }
      test("2", ignore = true) { counter2++ }
      test("3", ignore = true) { counter3++ }
      test("4", ignore = true) { counter4++ }
    }
    counter shouldEquals 1
    counter1 shouldEquals 0
    counter2 shouldEquals 0
    counter3 shouldEquals 0
    counter4 shouldEquals 0
  }
}
