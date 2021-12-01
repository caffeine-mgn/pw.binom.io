package pw.binom.io.httpServer.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.http.websocket.AbstractWebSocketConnection

class ServerWebSocketConnection(
    input: AsyncInput,
    output: AsyncOutput,
) : AbstractWebSocketConnection(
    input = input,
    output = output,
) {
    override val masking: Boolean
        get() = false
}