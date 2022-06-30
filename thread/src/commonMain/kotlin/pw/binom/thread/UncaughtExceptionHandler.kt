package pw.binom.thread

fun interface UncaughtExceptionHandler {
    fun uncaughtException(thread: Thread, throwable: Throwable)
}
