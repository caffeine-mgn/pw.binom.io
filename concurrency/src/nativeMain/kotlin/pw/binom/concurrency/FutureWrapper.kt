package pw.binom.concurrency

import pw.binom.Future
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
}

internal inline class FutureUnit(val native: NativeFuture<Unit>) : Future<Unit> {

    override val resultOrNull: Unit
        get() = native.result
    override val isSuccess: Boolean
        get() = true
    override val exceptionOrNull: Throwable?
        get() = null

    override val isDone: Boolean
        get() = native.state == FutureState.COMPUTED || native.state == FutureState.THROWN
}