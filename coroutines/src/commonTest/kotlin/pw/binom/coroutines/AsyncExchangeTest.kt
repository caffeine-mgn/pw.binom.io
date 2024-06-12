package pw.binom.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.testing.shouldEquals
import pw.binom.testing.shouldNull
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class AsyncExchangeTest {
  @Test
  fun pushBlockTest() = runTest {
    withContext(Dispatchers.Default) {
      val e = AsyncExchange<Int>()
      withTimeoutOrNull(2.seconds) {
        e.push(1)
        e.push(2)
      }.shouldNull()
    }
  }

  @Test
  fun pushWithDelayTest() = runTest {
    withContext(Dispatchers.Default) {
      val e = AsyncExchange<Int>()
      val popJob = GlobalScope.launch {
        e.extract() shouldEquals 42
      }
      val pushJob = GlobalScope.launch {
        delay(1.seconds)
        e.push(42)
      }
      popJob.join()
      pushJob.join()
    }
  }

  @Test
  fun extractWithDelayTest() = runTest {
    withContext(Dispatchers.Default) {
      val e = AsyncExchange<Int>()
      val popJob = GlobalScope.launch {
        delay(1.seconds)
        e.extract() shouldEquals 42
      }
      val pushJob = GlobalScope.launch {
        e.push(42)
      }
      popJob.join()
      pushJob.join()
    }
  }
}
