package pw.binom.thread

interface ExecutorService {
    val isShutdown: Boolean
    fun submit(f: () -> Unit)

    fun shutdownNow(): Collection<() -> Unit>
    fun isThreadFromPool(thread: Thread): Boolean
}
