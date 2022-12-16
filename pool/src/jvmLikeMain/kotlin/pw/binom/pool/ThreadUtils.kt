package pw.binom.pool

internal actual object ThreadUtils {
    actual val currentThreadId: Long = Thread.currentThread().id
}
