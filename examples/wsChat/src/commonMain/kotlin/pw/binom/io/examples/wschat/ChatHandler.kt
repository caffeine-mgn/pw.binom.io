package pw.binom.io.examples.wschat

import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader

class ChatHandler : WebSocketHandler() {

    private val clients = HashSet<WebSocketConnection>()
    private val forDelete = ArrayList<WebSocketConnection>()
    private suspend fun sendToClients(text: String, filter: (WebSocketConnection) -> Boolean = { true }) {

        clients.asSequence()
                .filter(filter)
                .forEach {
                    try {
                        it.write(MessageType.TEXT).utf8Appendable().use {
                            it.append(text)
                        }
                    } catch (e: WebSocketClosedException) {
                        it.close()
                        forDelete.add(it)
                    }
                }
        forDelete.forEach {
            clients.remove(it)
        }
        forDelete.clear()
    }

    override suspend fun connected(request: ConnectRequest) {
        val connection = request.accept()
        var name = request.headers["X-Nick-Name"]?.singleOrNull()
        if (name == null) {
            connection.write(MessageType.TEXT).utf8Appendable().use {
                it.append("Write you nick name")
            }
            name = connection.read().utf8Reader().use {
                it.readText().trim()
            }
        }
        sendToClients("$name has join to chat")
        clients += connection
        connection.incomeMessageListener = {
            try {
                val txt = it.read().utf8Reader().use {
                    it.readText().trim()
                }
                sendToClients("$name: $txt") { con -> it !== con }
            } catch (e: WebSocketClosedException) {
                clients.remove(it)
                sendToClients("$name has leave")
                throw e
            }
        }
    }
}