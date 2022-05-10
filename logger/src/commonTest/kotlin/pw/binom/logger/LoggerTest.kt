package pw.binom.logger

import kotlin.test.Test
import kotlin.test.assertSame

class LoggerTest {
    @Test
    fun testEq() {
        assertSame(Logger.getLogger("Test"), Logger.getLogger("Test"))

//        val w1 = Worker.create()
//        val r = w1.execute(Unit) {
//            Logger.getLogger("Test")
//        }
//        sleep(500)
//        assertSame(r.resultOrNull!!, Logger.getLogger("Test"))
//        w1.requestTermination()
    }
}
