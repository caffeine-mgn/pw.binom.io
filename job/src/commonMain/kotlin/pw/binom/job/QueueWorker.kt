package pw.binom.job

import pw.binom.Queue
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.neverFreeze

/**
 * Worker who takes tasks from list and processes them
 */
abstract class QueueWorker<T : Any> {
    internal lateinit var control: QueueThreadControl<T>

    /**
     * Does the worker has interrupted
     */
    val isInterrupted: Boolean
        get() = control.isInterrupted

    /**
     * interrupt the worker
     */
    fun interrupt() {
        control.interrupt()
    }

    /**
     * Returns one task from list
     */
    fun get() = control.stack.popLast()


    /**
     * Running process
     */
    abstract fun run()
}

class QueueThreadControl<T : Any> {
    private val interruptFlag = AtomicBoolean(false)

    val isInterrupted: Boolean
        get() = interruptFlag.value

    fun interrupt() {
        interruptFlag.value = true
    }

    internal val stack = Queue<T>()

    fun push(value: T) {
        stack.pushFirst(value)
    }

    init {
        doFreeze()
    }
}

class QueueThreadRunner<T : Any>(private val func: () -> QueueWorker<T>) : Closeable {


    init {
        neverFreeze()
    }

    val controller = QueueThreadControl<T>()
    private val worker = Worker()
    private var started = false
    private var promise: FuturePromise<Unit>? = null

    fun push(value: T) {
        controller.stack.pushFirst(value)
    }

    fun start(): FuturePromise<Unit> {
        if (started)
            throw IllegalStateException("Job already was started")
        started = true
        promise = worker.execute({
            val task = func()
            task.control = controller
            task
        }) {
            it.run()
            Unit
        }
        return promise!!
    }

    override fun close() {
        controller.interrupt()
        worker.close()
        promise?.join()
    }
}