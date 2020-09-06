package pw.binom

interface Future<T> {
    val resultOrNull: T?
    val isSuccess: Boolean
    val isFailure: Boolean
        get() = !isSuccess
    val exceptionOrNull: Throwable?
    val isDone:Boolean

    fun <R> consume(func: (Result<T>) -> R): R

    companion object {
        fun <T> success(result: T): Future<T> = SuccessFuture(result)
    }
}

private class SuccessFuture<T>(result: T) : Future<T> {
    override val resultOrNull: T? = result
    override val isSuccess: Boolean
        get() = true
    override val exceptionOrNull: Throwable?
        get() = null

    override fun <R> consume(func: (Result<T>) -> R): R = func(Result.success(resultOrNull as T))
    override val isDone: Boolean
        get() = true
}