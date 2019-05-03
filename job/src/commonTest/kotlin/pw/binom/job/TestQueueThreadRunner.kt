package pw.binom.job

import pw.binom.Thread
import pw.binom.atomic.AtomicInt
import kotlin.test.Test

class TestQueueThreadRunner {

    class SimpleJobData(value: Int) {
        val int = AtomicInt(value)
    }

    class Task : QueueWorker<SimpleJobData>() {
        override fun run() {
            while (!isInterrupted) {
                val item = get()
                if (item == null) {
                    Thread.sleep(100)
                    continue
                }
                item.int.increment()
            }
        }

    }

    @Test
    fun test() {
        val data = SimpleJobData(0)

        val runner = QueueThreadRunner { Task() }

        runner.start()
        runner.push(data)

        val start = Thread.currentTimeMillis()
        while (data.int.value == 0) {
            if (Thread.currentTimeMillis() - start > 10_000)
                throw AssertionError("Timeout")
            Thread.sleep(100)
        }
        runner.close()
    }
}