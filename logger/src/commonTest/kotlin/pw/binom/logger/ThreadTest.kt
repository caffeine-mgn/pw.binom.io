package pw.binom.logger

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import pw.binom.thread.Runnable
import pw.binom.thread.Thread
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
@SharedImmutable
val SimpleLevel = object : Logger.Level {
    override val name: String
        get() = "SIMPLE"
    override val priority: UInt
        get() = Logger.INFO.priority

}
*/
class ThreadTest {
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
    fun ff() {
        val done = AtomicInt(0)
        val thread = Thread(Runnable {
            Logger.getLog("Test").info("Test")
            done.increment()
        })

        val thread2 = Thread(Runnable {
            Logger.getLog("Test").info("Test")
            done.increment()
        })

        thread2.start()
        thread.start()

        thread.join()
        thread2.join()

        assertEquals(2, done.value)
    }
}