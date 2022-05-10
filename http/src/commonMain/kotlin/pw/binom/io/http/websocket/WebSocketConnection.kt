package pw.binom.io.http.websocket

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncOutput

interface WebSocketConnection : AsyncCloseable {

    /**
     * Read message. Must be call only from network thread
     */
    suspend fun read(): Message

    /**
     * Send message. Must be call only from network thread. After message finished AsyncOutput should be closed
     */
    suspend fun write(type: MessageType): AsyncOutput

//    /**
//     * Send message. Can be call from not only network thread
//     */
//    suspend fun write(type: MessageType, func: suspend (AsyncOutput) -> Unit)
}
