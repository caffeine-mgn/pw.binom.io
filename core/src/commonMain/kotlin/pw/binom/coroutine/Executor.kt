package pw.binom.coroutine

interface Executor {
    fun execute(func: suspend () -> Unit)
}