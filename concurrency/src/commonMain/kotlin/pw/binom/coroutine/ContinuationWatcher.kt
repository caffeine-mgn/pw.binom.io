package pw.binom.coroutine

import pw.binom.concurrency.DeadlineTimer
import kotlin.coroutines.Continuation
import kotlin.time.Duration.Companion.minutes

object ContinuationWatcher {
    private val instance = DeadlineTimer.create()
    fun <T> watch(continuation: Continuation<T>): Continuation<T> {
        val stack = Throwable().stackTraceToString()
        var done = false
        val newCon = object : Continuation<T> by continuation {
            override fun resumeWith(result: Result<T>) {
                done = true
            }
        }
        instance.delay(1.minutes) {
            if (!done) {
                println("---===Suspend corutine not resumed on 1 minues. Stack===---\n$stack---======---")
            }
        }
        return newCon
    }
}
