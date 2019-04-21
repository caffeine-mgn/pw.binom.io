package pw.binom

import kotlin.test.Test
import kotlin.test.assertTrue

class TestThreadUtils {

    @Test
    fun test() {
        val start = currentTimeMillis()
        sleep(500)
        val end = currentTimeMillis()

        assertTrue(end > start)
        assertTrue(end - start > 500 && end - start < 1000)

    }
}