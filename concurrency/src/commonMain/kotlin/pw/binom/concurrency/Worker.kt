package pw.binom.concurrency

import pw.binom.Future
import pw.binom.getOrException

expect class Worker : CrossThreadCoroutine, Executor {
    constructor(name: String? = null)

    fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT>
    fun requestTermination(): Future<Unit>
    val isInterrupted: Boolean
    val id: Long
    val taskCount: Int

    companion object {
        val current: Worker?
    }
}

expect fun Worker.Companion.sleep(deley: Long)
expect val Worker.Companion.availableProcessors: Int

fun <T> Future<T>.join() {
    while (!isDone) {
        Worker.sleep(1)
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