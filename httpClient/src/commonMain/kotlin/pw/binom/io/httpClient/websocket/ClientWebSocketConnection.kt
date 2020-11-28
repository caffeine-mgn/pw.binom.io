package pw.binom.io.httpClient.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.http.websocket.AbstractWebSocketConnection
import pw.binom.io.socket.nio.SocketNIOManager

class ClientWebSocketConnection(input: AsyncInput, output: AsyncOutput, rawConnection: SocketNIOManager.TcpConnectionRaw) : AbstractWebSocketConnection(
        input = input,
        output = output,
        rawConnection = rawConnection
) {
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