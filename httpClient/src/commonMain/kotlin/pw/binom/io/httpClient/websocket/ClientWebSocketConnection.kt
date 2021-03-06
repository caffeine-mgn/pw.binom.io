package pw.binom.io.httpClient.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.AsyncChannel
import pw.binom.io.http.websocket.AbstractWebSocketConnection

class ClientWebSocketConnection(input: AsyncInput, output: AsyncOutput, rawConnection: AsyncChannel) :
    AbstractWebSocketConnection(
        input = input,
        output = output,
        rawConnection = rawConnection
    ) {
    override val masking: Boolean
        get() = true

    override suspend fun asyncClose() {
        super.asyncClose()
        input.asyncClose()
        if (input !== output) {
            output.asyncClose()
        }
    }
}