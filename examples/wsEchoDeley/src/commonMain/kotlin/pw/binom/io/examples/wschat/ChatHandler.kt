package pw.binom.io.examples.wschat

import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.concurrency.Worker
import pw.binom.concurrency.execute
import pw.binom.concurrency.sleep
import pw.binom.io.*
import pw.binom.network.network

class ChatHandler : WebSocketHandler() {

    val worker = Worker()

    override suspend fun connected(request: ConnectRequest) {
        val connection = request.accept()
        connection.write(MessageType.TEXT).utf8Appendable().use {
            it.append("Write you message. I will send your message to you with delay 1 sec")
        }
        while (true) {
            connection.read().bufferedReader().use {
                val txt = it.readText()
                execute(worker) {
                    Worker.sleep(1000)
                    network {
                        connection.write(MessageType.TEXT).use {
                            it.bufferedWriter().use {
                                it.append("Echo: $txt")
                            }
                        }
                    }
                }
            }
        }
    }
}