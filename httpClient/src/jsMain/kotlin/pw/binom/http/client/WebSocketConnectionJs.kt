package pw.binom.http.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.BinaryType
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.files.Blob
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.http.websocket.WebSocketInput
import pw.binom.io.wrap
import pw.binom.toArrayBuffer

class WebSocketConnectionJs(val native: WebSocket) : WebSocketConnection {
  private var closed = false
  override val isReadReady: Boolean
    get() = !closed
  override val isWriteReady: Boolean
    get() = !closed

  private val incomeChannel = Channel<MessageEvent>()

  init {
    native.addEventListener(type = "message", callback = { event ->
      event as MessageEvent
      GlobalScope.launch { incomeChannel.send(event) }
    })
    native.onmessage
  }

  override suspend fun read(): WebSocketInput {
    if (closed) {
      throw ClosedException()
    }
    val event = incomeChannel.receive()
    val buffer = when (val data = event.data) {
      is String -> data.encodeToByteArray().wrap()
      is Blob -> Int8Array(data.toArrayBuffer())
      is ArrayBuffer -> ByteBuffer(data)
      else -> throw IllegalStateException("Unknown message type of $data")
    }
    TODO("Not yet implemented")
  }

  override suspend fun write(type: MessageType): AsyncOutput {
    if (closed) {
      throw ClosedException()
    }
    return when (type) {
      MessageType.BINARY -> WsMessageWriter(binary = true, native = native)
      MessageType.TEXT -> WsMessageWriter(binary = false, native = native)
      MessageType.CLOSE -> WsMessageCloseWriter(native = native) { closed = true }
      MessageType.CONTINUATION,
      MessageType.PING,
      MessageType.PONG,
        -> throw IllegalStateException("Message type $type")
    }
  }

  override suspend fun writeIfReady(type: MessageType): AsyncOutput? =
    write(type)

  override suspend fun asyncClose() {
    if (!closed) {
      native.close()
    }
  }
}
