package pw.binom.job

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FF(var value: Int)

class TestExecutor {
    @Test
    fun run() {
        val exe = Executor()


        var ff = FF(10)
        val func: (FF) -> FF = {
            FF(it.value + 2)
        }

        val e1 = exe.execute(ff, func)
        val e2 = exe.execute(ff, func)

        exe.close()

        assertTrue(e1.isFinished)
        assertTrue(e2.isFinished)
        assertEquals(12, e1.result.value)
        assertEquals(12, e2.result.value)
    }
}