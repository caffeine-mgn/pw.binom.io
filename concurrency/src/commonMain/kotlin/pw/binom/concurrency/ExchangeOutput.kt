package pw.binom.concurrency

interface ExchangeOutput<T : Any?> {
    fun put(value: T)
}