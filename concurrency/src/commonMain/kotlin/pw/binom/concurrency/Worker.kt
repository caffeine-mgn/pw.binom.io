package pw.binom.concurrency

import pw.binom.Future
import pw.binom.coroutine.Dispatcher

interface Worker : Dispatcher {
    companion object

    fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT>
    fun <RESULT> execute(func: () -> RESULT): Future<RESULT> =
        execute(Unit) { func() }

    fun requestTermination(): Future<Unit>
    val isInterrupted: Boolean
    val id: Long
    val taskCount: Int
}