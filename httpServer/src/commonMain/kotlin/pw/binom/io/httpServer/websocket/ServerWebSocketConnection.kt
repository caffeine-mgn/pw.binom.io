package pw.binom.io.httpServer.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.http.websocket.AbstractWebSocketConnection
import pw.binom.io.socket.nio.SocketNIOManager

class ServerWebSocketConnection(input: AsyncInput, output: AsyncOutput, rawConnection: SocketNIOManager.TcpConnectionRaw) : AbstractWebSocketConnection(
        input = input,
        output = output,
        rawConnection = rawConnection
) {
    override val masking: Boolean
        get() = false
}