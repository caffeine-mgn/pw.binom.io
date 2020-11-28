package pw.binom.logger

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.Worker
import kotlin.test.Test
import kotlin.test.assertEquals

class LoggerTest {
    /*
        class SimpleTask : Task() {

            private val log1 = Logger.getLog("SimpleTask")

            override fun execute() {
                log1.log(SimpleLevel, "Test")
            }

        }

        private val log2 = Logger.getLog("ROOT")

        @Test
        fun test() {
            Worker.execute { SimpleTask() }.also {
                it.join()
                it.worker.close()
            }
            log2.log(SimpleLevel, "Test")
        }
    */

    @Test
    fun threadTest2() {
        val w1 = Worker()
        val w2 = Worker()
        val logger = AtomicReference<Logger.LoggerImpl?>(null)
        val f1 = w1.execute(Unit) {
            try {
                logger.value = Logger.getLog("Test")
                12
            } catch (e:Throwable) {
                e.printStackTrace()
            }
        }
        f1.resultOrNull
        val f2 = w2.execute(Unit) {
            assertEquals(logger.value, Logger.getLog("Test"))
        }
        f2.resultOrNull
        assertEquals(logger.value, Logger.getLog("Test"))
    }

    @Test
    fun threadTest1() {
        val done = AtomicInt(0)
        val w1 = Worker()
        val w2 = Worker()
        val f1 = w1.execute(Unit) {
            Logger.getLog("Test").info("Test")
            done.increment()
        }
        val f2 = w2.execute(Unit) {
            Logger.getLog("Test").info("Test")
            done.increment()
        }

        f1.resultOrNull
        f2.resultOrNull

        assertEquals(2, done.value)
    }
}