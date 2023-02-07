package pw.binom.network

import pw.binom.io.socket.KeyListenFlags
import pw.binom.io.socket.Selector
import pw.binom.thread.Thread
import pw.binom.thread.UncaughtExceptionHandler
import kotlin.time.Duration

object SelectExecutor {

    fun startSelecting(
        selector: Selector,
        isSelectorClosed: () -> Boolean,
        submitTask: (() -> Unit) -> Unit,
        exceptionHandler: UncaughtExceptionHandler? = null
    ) {
        while (!isSelectorClosed()) {
            selector.select(timeout = Duration.INFINITE) { event ->
                try {
                    val attachment = event.key.attachment
                    if (attachment == null) {
                        println("Event: $event. attachment is null")
                        return@select
                    }
                    attachment ?: return@select // ?: error("Attachment is null")
                    val connection = attachment as AbstractConnection
//                    println("Event: $event")
                    if (event.key.readFlags and KeyListenFlags.ERROR != 0) {
                        submitTask {
                            connection.error()
                        }
                    }
                    if (event.key.readFlags and KeyListenFlags.WRITE != 0) {
                        submitTask {
                            connection.readyForWrite(event.key)
                        }
                    }
                    if (event.key.readFlags and KeyListenFlags.READ != 0) {
                        submitTask {
                            connection.readyForRead(event.key)
                        }
                    }
                } catch (e: Throwable) {
                    val handler = exceptionHandler ?: Thread.currentThread.uncaughtExceptionHandler
                    handler.uncaughtException(
                        thread = Thread.currentThread,
                        throwable = e,
                    )
                }
            }
        }
    }
}
