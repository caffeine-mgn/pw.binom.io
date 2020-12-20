package pw.binom.io.examples.wschat

import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep

class ChatHandler : WebSocketHandler() {

    val worker = Worker()

    override suspend fun connected(request: ConnectRequest) {
        val connection = request.accept()
        connection.write(MessageType.TEXT).utf8Appendable().use {
            it.append("Write you message. I will send your message to you with deley 1 sec")
        }
        while (true) {
            connection.read().utf8Reader().use {
                val txt = it.readText()
                worker.execute(connection to txt) { params ->
                    Worker.sleep(1000)
                    params.first.write(MessageType.TEXT) {
                        it.utf8Appendable().use {
                            it.append("Echo: ${params.second}")
                        }
                    }
                }
            }
        }
    }
}