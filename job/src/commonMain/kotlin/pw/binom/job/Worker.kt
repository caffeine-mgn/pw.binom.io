package pw.binom.job
/*
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.io.Closeable

expect class Worker : Closeable {
    constructor()

    fun <P, R> execute(param: () -> P, task: (P) -> R): FuturePromise<R>

    companion object
}

abstract class Task {
    internal lateinit var interruptFlag: AtomicBoolean
    abstract fun execute()

    val isInterrupted: Boolean
        get() = interruptFlag.value

    fun interrupt() {
        interruptFlag.value = true
    }
}

class WorkerHandler internal constructor(
        private val interruptFlag: AtomicBoolean,
        val worker: Worker,
        private val promise: FuturePromise<Unit>) {

    val isInterrupted: Boolean
        get() = interruptFlag.value

    fun interrupt() {
        interruptFlag.value = true
    }

    fun join() {
        promise.join()
    }
}

fun Worker.Companion.execute(func: () -> Task): WorkerHandler {
    val worker = Worker()
    return worker.execute(func)
}

fun Worker.execute(func: () -> Task): WorkerHandler {
    val interruptFlag = AtomicBoolean(false)
    interruptFlag.doFreeze()
    val promise = execute({
        val task = func()
        task.interruptFlag = interruptFlag
        task
    }) {
        it.execute()
    }
    return WorkerHandler(interruptFlag, this, promise)
}

 */