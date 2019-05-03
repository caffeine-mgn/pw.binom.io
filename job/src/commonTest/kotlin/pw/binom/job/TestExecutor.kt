package pw.binom.job

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FF(var value: Int)

class TestExecutor {
    @Test
    fun run() {
        val exe = Worker()

        var ff = FF(10)

        val func: (FF) -> FF = {
            val r = FF(it.value + 2)
            println("DONE!")
            r
        }

        val e1 = exe.execute({ ff }, func)
        val e2 = exe.execute({ ff }, func)

        exe.close()
        e1.join()
        e2.join()

        assertTrue(e1.isFinished)
        assertTrue(e2.isFinished)
        assertEquals(12, e1.result.value)
        assertEquals(12, e2.result.value)
    }
}