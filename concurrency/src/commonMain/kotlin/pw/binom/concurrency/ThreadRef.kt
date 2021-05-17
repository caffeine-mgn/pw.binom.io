package pw.binom.concurrency

import kotlin.jvm.JvmInline

/**
 * Object for check current thread and thread where [this] created
 */
@JvmInline
value class ThreadRef(private val threadId: Long = currentThreadId) {

    /**
     * Returns true if current thread is thread where [this] created
     */
    val same
        get() = currentThreadId == threadId

    fun checkSame(message: String? = null) {
        if (!same) {
            throw IllegalStateException(message)
        }
    }

    fun checkNotSame(message: String? = null) {
        if (!same) {
            throw IllegalStateException(message)
        }
    }
}

internal expect val currentThreadId: Long