package pw.binom.thread

interface ExecutorService {
    val isShutdown: Boolean
    val taskCount: Int
    fun submit(f: () -> Unit)

    fun shutdownNow(): Collection<() -> Unit>
    fun isThreadFromPool(thread: Thread): Boolean
}
