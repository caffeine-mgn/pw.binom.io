package pw.binom

import pw.binom.thread.Thread
import kotlin.test.Test
import kotlin.test.assertTrue

class TestThreadUtils {

    @Test
    fun test() {
        val start = Thread.currentTimeMillis()
        Thread.sleep(500)
        val end = Thread.currentTimeMillis()

        assertTrue(end > start)
        println("end - start = ${end - start}")
        assertTrue(end - start in 500..1000)
    }
}