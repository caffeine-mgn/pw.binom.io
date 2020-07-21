package pw.binom.io.httpClient.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.AsyncChannel
import pw.binom.io.http.websocket.AbstractWebSocketConnection

class ClientWebSocketConnection(input: AsyncInput, output: AsyncOutput) : AbstractWebSocketConnection(input = input, output = output) {
    override val masking: Boolean
        get() = true

    override suspend fun close() {
        super.close()
        input.close()
        if (input !== output) {
            output.close()
        }
    }
}