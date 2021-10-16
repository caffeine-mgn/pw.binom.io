package pw.binom.coroutine

interface AsyncExchangeInput<T> {
    suspend fun pop(): T
}