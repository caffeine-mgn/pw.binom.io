package pw.binom.concurrency

import pw.binom.Environment
import pw.binom.Platform
import pw.binom.platform
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class SleepTest {

    @Test
    fun sleepTest() {
        if (Environment.platform != Platform.JS) {
            return
        }
        val vv = measureTime {
            sleep(1500L)
        }
        assertTrue(vv.inWholeMilliseconds > 1400L)
        assertTrue(vv.inWholeMilliseconds < 1600L)
    }
}
