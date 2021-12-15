package pw.binom.concurrency

import kotlinx.coroutines.*
import pw.binom.Future
import pw.binom.coroutine.Dispatcher

expect class Worker(name: String? = null) : CoroutineDispatcher {
    companion object {
        val current: Worker?
    }

    fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT>
    fun requestTermination(): Future<Unit>
    val isInterrupted: Boolean
    val id: Long
    val taskCount: Int
}

fun <RESULT> Worker.execute(func: () -> RESULT): Future<RESULT> =
    execute(Unit) { func() }

internal expect val Worker.Companion.availableProcessors: Int

//fun Worker.startCoroutine(func: suspend CoroutineScope.() -> Unit) =
//    this.execute(this) { self ->
//        runBlocking(self) {
//            withContext(self, func)
//        }
//    }