package pw.binom.thread

class SingleThreadExecutorService : AbstractThreadExecutorService() {

    private val thread = Thread {
        while (!closed.getValue()) {
            queue.popBlocked().invoke()
        }
    }

    init {
        thread.start()
    }

    override fun joinAllThread() {
        thread.join()
    }

    override fun isThreadFromPool(thread: Thread): Boolean = thread.id == this.thread.id
}
