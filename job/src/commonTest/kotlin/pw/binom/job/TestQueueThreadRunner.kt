package pw.binom.job

import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestQueueThreadRunner {

    @Test
    fun workerWithQueue() {
        class TaskImpl(val input: Queue<Int>, val output: AppendableQueue<Int>) : pw.binom.job.Task() {
            override fun execute() {
                while (!isInterrupted) {
                    output.push(input.popAwait() * 10)
                    Thread.sleep(1)
                }
            }
        }

        val input = FreezedStack<Int>().asFiFoQueue()
        val output = FreezedStack<Int>().asFiFoQueue()

        val thread = Worker.execute{TaskImpl(input, output)}

        input.push(10)

        assertEquals(100, output.popAwait())
        thread.interrupt()
        thread.join()
    }
}