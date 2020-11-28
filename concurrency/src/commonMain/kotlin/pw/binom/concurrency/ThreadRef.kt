package pw.binom.concurrency

/**
 * Object for check current thread and thread where [this] created
 */
inline class ThreadRef(private val threadId: Long = currentThreadId) {

    /**
     * Returns true if current thread is thread where [this] created
     */
    val same
        get() = currentThreadId == threadId
}

internal expect val currentThreadId: Long