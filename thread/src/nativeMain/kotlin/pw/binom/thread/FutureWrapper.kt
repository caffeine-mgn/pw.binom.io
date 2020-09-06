package pw.binom.thread

import kotlinx.cinterop.StableRef
import pw.binom.Future
import pw.binom.io.Closeable
import kotlin.native.concurrent.FutureState
import kotlin.native.concurrent.Future as NativeFuture

inline class FutureWrapper<T>(val native: NativeFuture<Result<T>>) : Future<T> {

    override val resultOrNull: T?
        get() = native.result.getOrNull()
    override val isSuccess: Boolean
        get() = native.result.isSuccess
    override val exceptionOrNull: Throwable?
        get() = native.result.exceptionOrNull()

    override val isDone: Boolean
        get() = native.state == FutureState.COMPUTED || native.state == FutureState.THROWN


    override fun <R> consume(func: (Result<T>) -> R): R =
            func(native.result)
}

inline class FutureUnit(val native: NativeFuture<Unit>) : Future<Unit> {

    override val resultOrNull: Unit
        get() = native.result
    override val isSuccess: Boolean
        get() = true
    override val exceptionOrNull: Throwable?
        get() = null

    override fun <R> consume(func: (Result<Unit>) -> R): R =
            func(Result.success(native.result))

    override val isDone: Boolean
        get() = native.state == FutureState.COMPUTED || native.state == FutureState.THROWN
}