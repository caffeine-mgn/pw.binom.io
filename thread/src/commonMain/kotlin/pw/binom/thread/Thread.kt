package pw.binom.thread

expect open class Thread {
    constructor(runnable: Runnable)
    constructor()


    companion object {
        val currentThread: Thread

        /**
         * Block current thread on [time] milliseconds
         *
         * @param time time to block current thread. In milliseconds
         */
        fun sleep(time: Long)

        /**
         * Returns current time in milliseconds
         *
         * @return current time in milliseconds
         */
        fun currentTimeMillis(): Long
    }

    open fun start()
    val isInterrupted: Boolean
    fun interrupt()
    protected open fun run()
    open val id: Long
    fun join()
}

class InterruptedException : RuntimeException()