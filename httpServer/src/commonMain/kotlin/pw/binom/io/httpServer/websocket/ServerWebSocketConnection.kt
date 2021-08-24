package pw.binom.io.httpServer.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.AsyncChannel
import pw.binom.io.http.websocket.AbstractWebSocketConnection
import pw.binom.network.NetworkDispatcher

class ServerWebSocketConnection(
    input: AsyncInput,
    output: AsyncOutput,
    rawConnection: AsyncChannel,
    networkDispatcher: NetworkDispatcher,
) : AbstractWebSocketConnection(
    input = input,
    output = output,
    rawConnection = rawConnection,
    networkDispatcher = networkDispatcher
) {
    override val masking: Boolean
        get() = false
}