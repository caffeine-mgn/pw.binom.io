package pw.binom.concurrency

/**
 * Objects in [ExchangeOutput] will add as ObjectTree
 */
interface ExchangeOutput<T : Any?> {
    fun put(value: T)
}