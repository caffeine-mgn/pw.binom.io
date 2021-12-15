package pw.binom.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import pw.binom.Future
import pw.binom.coroutine.Dispatcher
import pw.binom.coroutine.Executor
import pw.binom.getOrException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

//expect class WorkerImpl : Executor, Worker, CoroutineDispatcher {
//    override fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>)
//    override fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT>
//    override fun requestTermination(): Future<Unit>
//    override val isInterrupted: Boolean
//    override val id: Long
//    override val taskCount: Int
//    fun <T> startCoroutine(
//        onDone: (Result<T>) -> Unit,
//        context: CoroutineContext = EmptyCoroutineContext,
//        func: suspend () -> T
//    )
//
//    companion object {
//        val current: WorkerImpl?
//    }
//}

internal data class CoroutineStartData<T>(
    val context: CoroutineContext,
    val func: suspend () -> T,
    val onDone: (Result<T>) -> Unit
)

fun <T> Future<T>.join() {
    while (!isDone) {
        sleep(1)
    }
}

fun <T> Future<T>.joinAndGetOrThrow(): T {
    join()
    return getOrException()
}

//class WorkerHolderElement(val worker: Worker) : CoroutineContext.Element {
//    override val key: CoroutineContext.Key<WorkerHolderElement>
//        get() = WorkerHolderElementKey
//}
//
//object WorkerHolderElementKey : CoroutineContext.Key<WorkerHolderElement>