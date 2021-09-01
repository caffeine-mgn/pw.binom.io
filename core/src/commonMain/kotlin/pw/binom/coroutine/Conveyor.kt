package pw.binom.coroutine

import pw.binom.io.AsyncCloseable

interface Conveyor<T> : AsyncCloseable {
    val isFinished: Boolean
    val exceptionOrNull: Throwable?
    suspend fun submit(value: T)
    suspend fun exception(exception: Throwable)
}