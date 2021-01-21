package pw.binom.concurrency

import pw.binom.Future
import kotlin.coroutines.CoroutineContext

expect class Worker {
    constructor(name: String? = null)

    fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT>
    fun requestTermination(): Future<Unit>
    val isInterrupted: Boolean
    val id: Long
    val taskCount:Int

    companion object {
        val current: Worker?
    }
}

expect fun Worker.Companion.sleep(deley: Long)

class WorkerHolderElement(val worker: Worker) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<WorkerHolderElement>
        get() = WorkerHolderElementKey
}

object WorkerHolderElementKey : CoroutineContext.Key<WorkerHolderElement>