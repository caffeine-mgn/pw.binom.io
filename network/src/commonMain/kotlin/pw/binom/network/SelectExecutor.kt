package pw.binom.network

import pw.binom.InternalLog
import pw.binom.io.socket.Selector
import pw.binom.thread.Thread
import pw.binom.thread.UncaughtExceptionHandler
import kotlin.time.Duration
import kotlin.time.TimeSource

object SelectExecutor {
  private val logger = InternalLog.file("SelectExecutor")
  fun startSelecting(
    selector: Selector,
    isSelectorClosed: () -> Boolean,
    submitTask: (() -> Unit) -> Unit,
    exceptionHandler: UncaughtExceptionHandler? = null,
  ) {
    while (!isSelectorClosed()) {
      val now = TimeSource.Monotonic.markNow()
      selector.select(timeout = Duration.INFINITE) { event ->
        logger.info(method = "startSelecting") { "Event.flags: ${event.flags}, event.key.readFlags: ${event.key.readFlags}" }
        try {
          val attachment = event.key.attachment ?: return@select
          //          attachment // ?: error("Attachment is null")
          val connection = attachment as AbstractConnection
          if (event.key.readFlags.isNotZero) {
            submitTask {
              connection.ready(key = event.key, flags = event.key.readFlags)
            }
          }
          if (event.key.readFlags.isError) {
            submitTask {
              connection.error()
            }
          }
          if (event.key.readFlags.isWrite) {
            submitTask {
              connection.readyForWrite(event.key)
            }
          }
          if (event.key.readFlags.isRead) {
            logger.info(method = "startSelecting") { "Submit read task" }
            submitTask {
              logger.info(method = "startSelecting") { "Call connection.readyForRead" }
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
      logger.info(method = "startSelecting") { "Select finished in ${now.elapsedNow()}" }
    }
  }
}
