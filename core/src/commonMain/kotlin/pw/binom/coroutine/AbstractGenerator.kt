package pw.binom.coroutine

import pw.binom.neverFreeze
import kotlin.coroutines.*

@Suppress("UNCHECKED_CAST")
private abstract class AbstractGenerator<T> : Generator<T> {
    private var finished = false
    private var yieldedValue: T? = null
    private var genContinuation: Continuation<Unit>? = null
    private var waitContinuation: Continuation<Unit>? = null
    private var generatorException: Throwable? = null
    protected open suspend fun yield(value: T) {
        yieldedValue = value
        suspendCoroutine<Unit> {
            genContinuation = it
            waitContinuation?.resume(Unit)
            waitContinuation = null
        }
    }

    override val isFinished: Boolean
        get() = finished

    protected abstract suspend fun execute(): T

    private val coroutine: suspend () -> T = {
        execute()
    }

    override suspend fun next(): T {
        if (finished) {
            throw NoSuchElementException()
        }
        if (generatorException != null) {
            throw generatorException!!
        }
        val crossCon = genContinuation
        if (crossCon != null) {
            suspendCoroutine<Unit> {
                waitContinuation = it
                crossCon.resumeWith(Result.success(Unit))
            }
        } else {
            val currentContext = suspendCoroutine<CoroutineContext> {
                it.resume(it.context)
            }
            suspendCoroutine<Unit> {
                waitContinuation = it
                coroutine.startCoroutine(object : Continuation<T> {
                    override val context: CoroutineContext = currentContext
                    override fun resumeWith(result: Result<T>) {
                        if (result.isFailure) {
                            generatorException = result.exceptionOrNull()!!
                            waitContinuation?.resumeWithException(generatorException!!)
                            waitContinuation = null
                            finished = true
                        } else {
                            finished = true
                            yieldedValue = result.getOrNull()
                            waitContinuation?.resume(Unit)
                            waitContinuation = null
                        }
                    }
                })
            }
        }
        return yieldedValue as T
    }

    init {
        neverFreeze()
    }
}

suspend fun <T : Any> generator(func: suspend Yieldable<T>.() -> T): Generator<T> {
    return object : AbstractGenerator<T>(), Yieldable<T> {
        override suspend fun execute(): T = this.func()
        override suspend fun yield(value: T) {
            super.yield(value)
        }
    }
}

interface Yieldable<T> {
    suspend fun yield(value: T)
}