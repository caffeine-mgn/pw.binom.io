package pw.binom.thread

object DefaultUncaughtExceptionHandler : UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        println("UncaughtException on ${thread.name} (${thread.id}):\n${throwable.stackTraceToString()}")
    }
}
