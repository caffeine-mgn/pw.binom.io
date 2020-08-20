package pw.binom.thread

interface Future<T> {
    val resultOrNull: T?
    val isSuccess: Boolean
    val isFailure: Boolean
        get() = !isSuccess
    val exceptionOrNull: Throwable?

    fun <R> consume(func: (Result<T>) -> R): R
}