package pw.binom.coroutine

interface ExchangeOutput<T> {
    fun push(value: T)
}