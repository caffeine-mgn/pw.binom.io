package pw.binom.thread

class FixedThreadExecutorService(
    threadCount: Int,
    val threadFactory: ((Thread) -> Unit) -> Thread = { func -> Thread(func = func) },
) : AbstractThreadExecutorService() {
    init {
        require(threadCount >= 1) { "threadCount should be more or equals 1" }
    }

    private val threadFunc: (Thread) -> Unit = { _ ->
        while (!closed.getValue()) {
            queue.popBlocked().invoke()
        }
    }

    private val threads = Array(threadCount) {
        threadFactory(threadFunc)
    }

    private val allThreadIds = HashSet<Long>()

    init {
        threads.forEach {
            it.start()
            allThreadIds += it.id
        }
    }

    override fun joinAllThread() {
        threads.forEach {
            it.join()
        }
    }

    override fun isThreadFromPool(thread: Thread): Boolean = thread.id in allThreadIds
}
