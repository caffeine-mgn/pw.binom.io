package pw.binom.thread

expect class ThreadLocal<T> {
    constructor()

    fun get(): T?
    fun set(value: T)
    fun remove()
}
