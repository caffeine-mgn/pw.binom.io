package pw.binom

import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import kotlin.test.Test
import kotlin.test.assertEquals

class FreezedStackTest {

    @Test
    fun threding() {
        class SomeTask(val input: Queue<Int>, val output: AppendableQueue<Int>) : Task() {
            override fun execute() {
                while (!isInterrupted) {
                    input.popAwait(5_000)
                    val value = input.popOrNull()
                    if (value == null) {
                        Thread.sleep(10)
                        continue
                    }
                    output.push(value * value)
                }
            }
        }

        val output = FreezedStack<Int>().asFiFoQueue()
        val input = FreezedStack<Int>().asFiFoQueue()

        val w = Worker.execute { SomeTask(input, output) }

        fun test(value: Int) {
            input.push(value)
            assertEquals(value * value, output.popAwait(5_000))
        }

        test(10)
        test(20)
        test(30)
        test(40)
        test(50)

        w.interrupt()
    }
}