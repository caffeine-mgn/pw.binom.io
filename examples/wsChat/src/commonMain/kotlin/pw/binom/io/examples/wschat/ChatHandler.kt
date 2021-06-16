package pw.binom.io.examples.wschat

import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader

class ChatHandler : Handler {

    private val clients = HashSet<WebSocketConnection>()
    private suspend fun sendToClients(text: String, currentClient: WebSocketConnection) {
        val forDelete = ArrayList<WebSocketConnection>()
        clients.asSequence()
            .filter { it !== currentClient }
            .forEach {
                try {
                    it.write(MessageType.TEXT).utf8Appendable().use {
                        it.append(text)
                    }
                } catch (e: WebSocketClosedException) {
                    forDelete.add(it)
                    it.asyncClose()
                }
            }
        forDelete.forEach {
            clients.remove(it)
        }
    }

    override suspend fun request(req: HttpRequest) {
        val connection = req.acceptWebsocket()
        var name = req.headers["X-Nick-Name"]?.singleOrNull()
        if (name == null) {
            connection.write(MessageType.TEXT).utf8Appendable().use {
                it.append("Write you nick name")
            }
            name = connection.read().utf8Reader().use {
                it.readText().trim()
            }
        }
        clients += connection
        println("Client connected $name")
        while (true) {
            connection.read().utf8Reader().use {
                val txt = "$name: ${it.readText()}"
                println(txt)
                sendToClients(txt, connection)
            }
        }
    }
}