package pw.binom.concurrency

import pw.binom.Future
import java.util.concurrent.Future as JFuture

inline class FutureWrapper<T>(val future: JFuture<Result<T>>) : Future<T> {
    override val resultOrNull: T?
        get() = future.get()?.getOrNull()

    override val isSuccess: Boolean
        get() = future.get()?.isSuccess ?: true

    override val isFailure: Boolean
        get() = !isSuccess

    override val exceptionOrNull: Throwable?
        get() = future.get()?.exceptionOrNull()

    override val isDone: Boolean
        get() = future.isDone

    override fun <R> consume(func: (Result<T>) -> R): R = func(future.get())
}

fun <T> JFuture<Result<T>>.wrap() = FutureWrapper(this)