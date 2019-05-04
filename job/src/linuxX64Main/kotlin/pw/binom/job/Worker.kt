package pw.binom.job

import pw.binom.io.Closeable
import kotlin.native.concurrent.Future
import kotlin.native.concurrent.FutureState
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.Worker as NWorker

actual class Worker : Closeable {
    override fun close() {
        native.requestTermination(true)
    }

    private val native = NWorker.start()

    private class Task<P, R>(val param: P, val task: (P) -> R)

    actual fun <P, R> execute(param: () -> P, task: (P) -> R): FuturePromise<R> =
            FuturePromiseImpl(native.execute(TransferMode.SAFE, { Task(param(), task).freeze() }) {
                it.task(it.param)
            })

    actual companion object
}

private class FuturePromiseImpl<T>(private val future: Future<T>) : FuturePromise<T> {
    override val isFinished: Boolean
        get() = future.state == FutureState.COMPUTED || future.state == FutureState.THROWN

    override val isError: Boolean
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise is not done")
            return future.state == FutureState.THROWN
        }

    override val result: T
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise is not done")

            if (future.state != FutureState.COMPUTED)
                throw IllegalStateException("Promise not returns Result")

            return future.result
        }

    override val exception: Throwable
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise is not done")

            if (future.state != FutureState.THROWN)
                throw IllegalStateException("Promise not throws Result")

            return future.result as Throwable
        }
}