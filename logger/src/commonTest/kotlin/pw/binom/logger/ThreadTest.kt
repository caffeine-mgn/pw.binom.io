package pw.binom.logger

import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.Test


@SharedImmutable
val SimpleLevel = object : Logger.Level {
    override val name: String
        get() = "SIMPLE"
    override val priority: UInt
        get() = Logger.INFO.priority

}

class ThreadTest {

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
}