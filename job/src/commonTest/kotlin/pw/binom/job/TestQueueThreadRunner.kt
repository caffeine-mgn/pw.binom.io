package pw.binom.job

import pw.binom.FreezedStack
import pw.binom.Queue
import pw.binom.Thread
import kotlin.test.Test
import kotlin.test.assertEquals

class TestQueueThreadRunner {

    @Test
    fun workerWithQueue() {
        class JobData(val value: Int, val promise: Promise<Int>)

        class TaskImpl(val input: Queue<JobData>) : pw.binom.job.Task() {
            override fun execute() {
                while (!isInterrupted) {
                    if (!input.isEmpty) {
                        val data = input.pop()
                        data.promise.resume(data.value * 10)
                    }
                    Thread.sleep(1)
                }
            }
        }


        val input = FreezedStack<JobData>().asFiFoQueue()

        val thread = Worker.execute { TaskImpl(input) }
        val promise = Promise<Int>()
        input.push(JobData(10, promise))
        val gg = promise.await()
        assertEquals(100, gg)
        thread.interrupt()
        thread.join()
    }
}