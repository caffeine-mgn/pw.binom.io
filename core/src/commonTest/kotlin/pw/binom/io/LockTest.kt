package pw.binom.io

import pw.binom.thread.Lock
import pw.binom.thread.use
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