package pw.binom

fun interface AsyncSupplier<T> {
    suspend fun get(): T
}

fun <T, R> AsyncSupplier<T>.map(func: suspend (T) -> R) = AsyncSupplier {
    func(get())
}
