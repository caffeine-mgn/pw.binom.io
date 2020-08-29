package pw.binom.io.http.websocket

import pw.binom.AsyncOutput
import pw.binom.io.AsyncCloseable

interface WebSocketConnection : AsyncCloseable {
    suspend fun read(): Message
    suspend fun write(type: MessageType): AsyncOutput
    var incomeMessageListener: (suspend (WebSocketConnection) -> Unit)?
}