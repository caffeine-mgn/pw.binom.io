package pw.binom.io.http.websocket

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pw.binom.InternalLog
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class WebSocketOutput(
  messageType: MessageType,
  masked: Boolean,
  val stream: AsyncOutput,
  bufferSize: Int,
  val connection: WebSocketConnectionImpl,
) : AsyncOutput {

  private val output = if (masked) {
    MaskedFrameOutput(
      messageType = messageType,
      stream = stream,
      bufferSize = bufferSize,
    )
  } else {
    UnmaskedFrameOutput(
      messageType = messageType,
      stream = stream,
      bufferSize = bufferSize,
    )
  }


  private var closing = AtomicBoolean(false)


  override suspend fun flush() {
    WsDetectSlow("WebSocketOutput::flush #0") {
      output.flush()
    }
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize =
    output.write(data)

  override suspend fun asyncClose() {
    if (!closing.compareAndSet(false, true)) {
      return
    }
    WsDetectSlow("WebSocketOutput::asyncClose #0 stream=${stream::class}") {
      try {
        WsDetectSlow("WebSocketOutput::asyncClose #1 stream=${stream::class}") {
          output.asyncClose()
        }
      } finally {
        WsDetectSlow("WebSocketOutput::asyncClose #2 stream=${stream::class}") {
          connection.writingMessageFinished()
        }
      }
    }
  }
}


inline fun <T> WsDetectSlow(msg: String, duration: Duration = 1.seconds, func: () -> T): T {
  val stackTrace = Throwable()
  val finished = AtomicBoolean(false)
  GlobalScope.launch {
    delay(duration)
    if (!finished.getValue()) {
      InternalLog.warn(file = "WS") { "Slow: $msg\n${stackTrace.stackTraceToString()}" }
      println("WS---->Slow: $msg\n${stackTrace.stackTraceToString()}")
    }
  }

  return try {
    func()
  } finally {
    finished.setValue(true)
  }
}
