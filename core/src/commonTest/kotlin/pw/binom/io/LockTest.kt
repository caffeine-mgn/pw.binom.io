package pw.binom.io

import pw.binom.AppendableQueue
import pw.binom.Lock
import pw.binom.atomic.AtomicInt
import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import pw.binom.use
import kotlin.test.Test

class LockTest {

    @Test
    fun testRelock() {
        val l = Lock()

        l.use {
            l.use {

            }
        }
    }
}