package pw.binom.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.atomic.AtomicInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class AsyncLazyTest {
  @Test
  fun getOnceTest() =
    runTest {
      var counter = AtomicInt(0)
      val value =
        AsyncLazy {
          counter.increment()
          0
        }

      repeat(10) {
        value.get()
        assertEquals(1, counter.getValue())
      }
    }

  @Test
  fun getAwait() =
    runTest {
      withContext(Dispatchers.Default) {
        val value = AtomicInt(0)
        val asyncValue =
          AsyncLazy {
            delay(2.seconds)
            value.addAndGet(1)
          }

        val durations =
          (0..9).map {
            GlobalScope.async { measureTime { asyncValue.get() } }
          }.awaitAll()
        durations.forEach {
          assertTrue(it > 2.seconds && it < 2.5.seconds)
        }
        assertEquals(1, value.getValue())
      }
    }

  @Test
  fun resetTest() =
    runTest {
      val value = AtomicInt(0)
      val asyncValue =
        AsyncLazy {
          value.addAndGet(1)
        }
      assertEquals(1, asyncValue.get())
      assertEquals(1, value.getValue())
      assertEquals(1, asyncValue.get())
      asyncValue.reset()
      assertEquals(2, asyncValue.get())
      assertEquals(2, value.getValue())
      assertEquals(2, asyncValue.get())
    }

  @Test
  fun resetWithCancel() =
    runTest {
      withContext(Dispatchers.Default) {
        val value = AtomicInt(0)
        val asyncValue =
          AsyncLazy {
            try {
              delay(2.seconds)
              value.addAndGet(1)
            } catch (e: Throwable) {
              e.printStackTrace()
              throw e
            }
          }

        val tasks =
          (0..10).map {
            GlobalScope.async { asyncValue.get() }
          }
        delay(1.seconds)
        tasks.forEach {
          assertTrue(it.isActive)
          assertFalse(it.isCancelled)
          assertFalse(it.isCompleted)
        }
        asyncValue.reset()
        delay(1.seconds)
        tasks.forEachIndexed { index, deferred ->
          println("$index isCancelled=${deferred.isCancelled} isActive=${deferred.isActive}")
        }
        tasks.forEach {
          assertTrue(it.isCancelled, "task not cancelled")
          assertFalse(it.isActive, "task steel active")
        }
      }
    }
}
