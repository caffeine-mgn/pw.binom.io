package pw.binom.job
/*
import pw.binom.FreezedStack
import pw.binom.Queue
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.neverFreeze

class FixedThreadPool<P, R>(size: Int, taskExecutor: () -> WorkerExecutor<P, R>) : Executor<P, R> {
    init {
        neverFreeze()
    }

    class Data<P, R>(val value: P, private val promise: ResumableFuturePromise<R>?) {

        fun resolve(value: R) {
            promise?.resume(value)
        }

        fun exception(exception: Throwable) {
            promise?.exception(exception)
        }
    }

    class Controller<P, R>(val queue: Queue<Data<P, R>>, val taskExecutor: () -> WorkerExecutor<P, R>) {

        private val _interrupted = AtomicBoolean(false)


        val isInterrupted: Boolean
            get() = _interrupted.value

        fun interrupt() {
            if (isInterrupted)
                throw IllegalStateException("Controller already interrupted")
            _interrupted.value = true
        }

        init {
            doFreeze()
        }
    }

    interface WorkerExecutor<P, R> {
        fun start(controller: Controller<P, R>)
    }

    private val queue = FreezedStack<Data<P, R>>().asFiFoQueue()

    private val controller = Controller(queue, taskExecutor)

    override fun execute(value: P, promise: ResumableFuturePromise<R>?) {
        queue.push(Data(value, promise))
    }

    override fun close() {
        controller.interrupt()
        workers.forEach {
            it.close()
        }
    }

    private val workers = Array(size) {
        val w = Worker()
        w.execute({ controller }) {
            it.taskExecutor().start(it)
        }
        w
    }
}
 */