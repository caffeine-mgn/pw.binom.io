package pw.binom.concurrency

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class WorkerTest {

    @Test
    fun sleepTest() {
        val vv = measureTime {
            sleep(1500L)
        }
        assertTrue(vv.inWholeMilliseconds > 1400L)
        assertTrue(vv.inWholeMilliseconds < 1600L)
    }
}