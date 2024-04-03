package pw.binom.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class WaitFirstTest {

  fun aa()= runTest{
    listOf(async { 1 }).awaitAll()
  }

  @Test
  fun waitValue() = runTest {
    withContext(Dispatchers.Default) {
      var cancelled = false
      val e = listOf(
        async {
          try {
            delay(3.seconds)
            10
          } catch (e: Throwable) {
            cancelled = true
            throw e
          }
        },
        async {
          delay(1.seconds)
          12
        },
      ).awaitFirst()
      delay(1.seconds)
      assertTrue(cancelled)
      assertTrue(e.isSuccess)
      assertEquals(e.getOrThrow(), 12)
    }
  }

  @Test
  fun emptyListTest() = runTest {
    val r = listOf<Deferred<Int>>().awaitFirst()
    assertFalse(r.isSuccess)
  }

  class TestException : RuntimeException()

  @Test
  fun throwExceptionTest() = try {
    runTest {
      listOf(
        async {
          throw TestException()
        },
      ).awaitFirst()
      fail()
    }
  } catch (e: TestException) {
    // Do nothing
  }

  @Test
  fun noElementByFilter() = runTest {
    withContext(Dispatchers.Default) {
      val r = listOf(
        async {
          delay(1.seconds)
          5
        },
        async {
          delay(2.seconds)
          20
        },
      ).awaitFirst {
        false
      }
      assertFalse(r.isSuccess)
    }
  }

  @Test
  fun waitValueWithFilter() = runTest {
    withContext(Dispatchers.Default) {
      val r = listOf(
        async {
          delay(1.seconds)
          5
        },
        async {
          delay(2.seconds)
          20
        },
      ).awaitFirst {
        it.isSuccess && it.getOrThrow() > 10
      }
      assertTrue(r.isSuccess)
      assertEquals(20, r.getOrThrow())
    }
  }
}
