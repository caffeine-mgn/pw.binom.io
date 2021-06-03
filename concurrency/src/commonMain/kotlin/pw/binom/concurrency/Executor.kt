package pw.binom.concurrency

interface Executor {
    fun execute(func: suspend () -> Unit)
}