package pw.binom.coroutine

@Deprecated("Not use")
interface Executor {
    @Deprecated("Not use")
    fun execute(func: suspend () -> Unit)
}